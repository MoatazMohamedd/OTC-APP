package app.checktdedno.otc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.unity3d.player.UnityPlayerActivity;

public class DetectedEmotion extends AppCompatActivity {

    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_emotion);

        title = findViewById(R.id.title);

        Intent intent = getIntent();
        String getEmotion = intent.getStringExtra("emotion");

        Intent intent2 = new Intent(this, UnityPlayerActivity.class);
        intent2.putExtra("msg", getEmotion);


        startActivity(intent2);


        title.setText(getEmotion);
    }
}