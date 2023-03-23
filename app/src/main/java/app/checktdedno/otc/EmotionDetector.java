package app.checktdedno.otc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import app.checktdedno.otc.ml.Model;

public class EmotionDetector {
    private Context context;
    private String[] classes = {"Angry", "Disgusted", "Fearful", "Happy", "Neutral", "Sad", "Surprised"};


    public EmotionDetector(Context context) {
        this.context = context;
    }

    int getMax(float[] array) {
        int maxIndex = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > array[maxIndex])
                maxIndex = i;
        }
        return maxIndex;
    }

    private ByteBuffer getByteBuffer(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ByteBuffer mImgData = ByteBuffer
                .allocateDirect(4 * width * height);
        mImgData.order(ByteOrder.nativeOrder());

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels)
            mImgData.putFloat((float) Color.red(pixel));

        return mImgData;
    }

    private ByteBuffer processImage(Bitmap unscaledRGB) {
        Bitmap resized = Bitmap.createScaledBitmap(unscaledRGB, 48, 48, true);
        ByteBuffer resizedGrey = getByteBuffer(resized);

        return resizedGrey;
    }

    String predictEmotion(Bitmap detectedFace) {

        int maxPosition = 0;
        try {
            Model model = Model.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 48, 48, 1}, DataType.FLOAT32);

            ByteBuffer byteBuffer = processImage(detectedFace);       // gets the image ready to be predicted

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();   //confidence array
            maxPosition = getMax(confidences);                       //gets the highest confidence

            // Releases model resources if no longer used.
            model.close();

        } catch (IOException e) {
            // TODO Handle the exception
        }
        return classes[maxPosition];
    }
}