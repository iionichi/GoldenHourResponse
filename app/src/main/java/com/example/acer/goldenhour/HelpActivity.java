package com.example.acer.goldenhour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpActivity extends AppCompatActivity {

    Button accidentVideo, anxietyVideo, cardiacVideo, seizureVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        accidentVideo = findViewById(R.id.accidentVideo);
        anxietyVideo = findViewById(R.id.anxietyVideo);
        cardiacVideo = findViewById(R.id.cardiacVideo);
        seizureVideo = findViewById(R.id.seizureVideo);

        accidentVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpActivity.this, AccidentVideoActivity.class);
                startActivity(intent);
            }
        });

        anxietyVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpActivity.this, AnxietyVideoActivity.class);
                startActivity(intent);
            }
        });

        cardiacVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpActivity.this, CardiacVideoActivity.class);
                startActivity(intent);
            }
        });

        seizureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpActivity.this, SeizureVideoActivity.class);
                startActivity(intent);
            }
        });
    }
}
