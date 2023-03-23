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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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

    private int[] location;

    private FrameLayout frameLayout;

    public static float left, top, bottom, right;

    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location = new int[2];

        frameLayout = findViewById(R.id.container);
        frameLayout.getLocationOnScreen(location);


        previewView = findViewById(R.id.previewView);


        emotionDetector = new EmotionDetector(this);

        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //  getSupportActionBar().hide();

        imageView = findViewById(R.id.imageView);

        Canvas canvas = new Canvas();
        canvas.drawColor(Color.CYAN);
        Paint p = new Paint();
// smooths
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4.5f);
        canvas.drawRect(20, 10, 30, 30, p);


        // Initialize the face detector
        faceDetector = FaceDetection.getClient();
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
                                                                Rect detectedFaceBoundingBox = faces.get(0).getBoundingBox();


                                                                if (isPortraitMode()) {
                                                                    // Swap width and height sizes when in portrait, since it will be rotated by
                                                                    // 90 degrees. The camera preview and the image being processed have the same size.
                                                                    graphicOverlay.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), true);
                                                                } else {
                                                                    graphicOverlay.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), true);
                                                                }


                                                                for (Face face : faces) {
                                                                    graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
                                                                }
                                                                graphicOverlay.postInvalidate();

                                                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                                                int height = displayMetrics.heightPixels;
                                                                int width = displayMetrics.widthPixels;


//                                                                Log.e("MAinActivity", "Y coordinate of frame layout" + String.valueOf(location[1]));
//                                                                Log.e("MainActivity", "Detected Face Left point: " + detectedFaceBoundingBox.left);
//                                                                Log.e("MainActivity", "Detected Face Top point: " + detectedFaceBoundingBox.top);

                                                                Face detectedFace = faces.get(0);
                                                                int newX = (int) left;
                                                                int newY = (int) top;
                                                                int newWidth = detectedFace.getBoundingBox().width();
                                                                int newHeight = detectedFace.getBoundingBox().height();
//                                                                Bitmap croppedFace = Bitmap.createBitmap(previewView.getBitmap(), faces.get(0).getBoundingBox().left, faces.get(0).getBoundingBox().bottom*2, detectedFaceBoundingBox.width()*2, detectedFaceBoundingBox.height()*2);
                                                                if (newX + detectedFaceBoundingBox.width() <= width && newY + detectedFaceBoundingBox.height() <= height && newX > 0 && newY > 0){
                                                                    Bitmap croppedFace = Bitmap.createBitmap(previewView.getBitmap(), newX, newY, 700, 700);
                                                                    Log.e("MainActivity", emotionDetector.predictEmotion(croppedFace));
                                                                    imageView.setImageBitmap(croppedFace);
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
                                                            Toast.makeText(MainActivity.this, "Error yad", Toast.LENGTH_SHORT).show();
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
}