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
    //_________________________________________________//
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
    //_________________________________________________//
    private ByteBuffer processImage(Bitmap unscaledRGB) {
        Bitmap resized = Bitmap.createScaledBitmap(unscaledRGB, 48, 48, true);
        ByteBuffer resizedGrey = getByteBuffer(resized);

        return resizedGrey;
    }
    //_________________________________________________//
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
    //_________________________________________________________________________________________________//
    //___________FINAL EMOTION CLASSIFICATION___________//
    int  getmaxInt(int[] array)
    {
        int maxIndex=0;
        for(int i =0;i<array.length;i++)
        {
            if(array[i] > array[maxIndex])
                maxIndex=i;
        }
        return maxIndex;
    }
    //_________________________________________________//
    void zerofy(int[] array)
    {
        for(int i=0;i<array.length;i++)
        {
            array[i]=0;
        }
    }
    //_________________________________________________//
    private int[] identifyScores(int[] setOfDetectedEmotions,int threshold)
    {   //Angry = 1;        @index 0 in scores
        //Happy = 2;        @index 1 in scores
        //Neutral = 3;      @index 2 in scores
        //Sad = 4;          @index 3 in scores

        int[] scores = new int[4];
        zerofy(scores);

        for(int i=0;i<threshold;i++)
        {
            if (setOfDetectedEmotions[i] == 1)
                scores[0] ++;

            else if (setOfDetectedEmotions[i] == 2)
                scores[1] ++;

            else if (setOfDetectedEmotions[i] == 3)
                scores[2] ++;

            else if (setOfDetectedEmotions[i] == 4)
                scores[3] ++;

        }
        return scores;
    }
    //_________________________________________________//
    String extractOneEmotion(int[] setOfDetectedEmotions , int threshold)
    {
        String[] classes = {"Angry","Happy","Neutral","Sad"};
        int[] scores = identifyScores(setOfDetectedEmotions,threshold);
        int maxIndex = getmaxInt(scores);

        return classes[maxIndex];
    }
    //_________________________________________________//
    int findMatch(String emotionDetected)
    {
        int found =0;
        String[] classes = {"Angry","Happy","Neutral","Sad"};
        for(int i=0;i<classes.length;i++)
        {
            if (emotionDetected==classes[i])
            {
                found=1;
                break;
            }
        }
            return found;
    }
    //_________________________________________________//
    int Integerize(String emotionDetected)
    {
        int emotion=0;

        if(emotionDetected=="Angry")
            emotion=1;
        else if (emotionDetected=="Happy")
            emotion=2;
        else if (emotionDetected=="Neutral")
            emotion=3;
        else if (emotionDetected=="Sad")
            emotion=4;

        return emotion;
    }
    //_________________________________________________//

}