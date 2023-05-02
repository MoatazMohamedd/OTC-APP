package app.checktdedno.otc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import android.content.res.Configuration;
import android.graphics.Bitmap;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageAnalysis imageAnalysis;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FaceDetector faceDetector;
    private EmotionDetector emotionDetector;

    private GraphicOverlay graphicOverlay;

    private ImageView imageView;


    public static float left, top, bottom, right;

    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getViews();

        //Start detection clients
        emotionDetector = new EmotionDetector(this);
        faceDetector = FaceDetection.getClient();

        //Detecting available cameras
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        //UI adjustments
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();


        startCamera();
    }

    @ExperimentalGetImage
    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the preview use case
                Preview preview = new Preview.Builder().setTargetResolution(new Size(640, 480)).build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());


                // Set up the image analysis use case
                imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();


                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        Log.e("MainActivity", "Frame received");

                        Image mediaImage = imageProxy.getImage();


                        if (mediaImage != null) {
                            InputImage image =
                                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                            // Pass image to an ML Kit Vision API
                            // ...
                            Task<List<Face>> result =
                                    faceDetector.process(image)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<List<Face>>() {
                                                        @Override
                                                        public void onSuccess(List<Face> faces) {
                                                            // Task completed successfully
                                                            // ...
                                                            if (faces.size() > 0) {
                                                                graphicOverlay.clear();


                                                                if (isPortraitMode()) {
                                                                    // Swap width and height sizes when in portrait, since it will be rotated by
                                                                    // 90 degrees. The camera preview and the image being processed have the same size.
                                                                    graphicOverlay.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), true);
                                                                }


                                                                for (Face face : faces) {
                                                                    graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
                                                                }
                                                                graphicOverlay.postInvalidate();


                                                                if (left > 0 && top > 0) {
                                                                    try {
                                                                        Bitmap croppedFace = Bitmap.createBitmap(previewView.getBitmap(), (int) left, (int) top, 560, 560);

                                                                        Log.e("MainActivity", emotionDetector.predictEmotion(croppedFace));

                                                                        imageView.setImageBitmap(croppedFace);

                                                                    } catch (
                                                                            IllegalArgumentException exception) {
                                                                        Toast.makeText(MainActivity.this, "Please stay within camera limits!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }


                                                        }
                                                    })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Task failed with an exception
                                                            // ...
                                                            Toast.makeText(MainActivity.this, "Couldn't detect any faces", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                                @Override
                                                public void onComplete(@NonNull Task<List<Face>> task) {
                                                    imageProxy.close();
                                                }
                                            });
                        }
                    }
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();

                // Bind the use cases to the camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                // Handle errors
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean isPortraitMode() {
        return getApplicationContext().getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_LANDSCAPE;
    }

    private void getViews() {
        previewView = findViewById(R.id.previewView);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        imageView = findViewById(R.id.imageView);
    }
}