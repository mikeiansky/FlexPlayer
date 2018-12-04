package com.demo.flexplayer;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.winson.flexplayer.FlexPlayerController;
import com.winson.flexplayer.FlexPlayerView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //    String TEST_PATH = "https://scb.liaidi.com//data//video//2017//12//20171214235251279358.mp4";
//    String TEST_PATH = "https://scb.liaidi.com//data//video//2017//12//20171214235251279358.mp4";
    String TEST_PATH = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
    String TEST_PATH2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testmp4.mp4";
    String TEST_PATH3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apeng/test.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FlexPlayerView flexPlayerView = findViewById(R.id.flex_player);
        Button play = findViewById(R.id.http_path);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flexPlayerView.setUp(v.getContext(), TEST_PATH);
            }
        });

        Button enterFullScreen = findViewById(R.id.local_path);
        enterFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flexPlayerView.setUp(v.getContext(), TEST_PATH2);

            }
        });

        findViewById(R.id.size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flexPlayerView.setUp(v.getContext(), TEST_PATH3);
            }
        });

//        flexPlayerView.setContainerBackground(getResources().getDrawable(R.drawable.white_background));


    }
}
