package com.demo.flexplayer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.winson.flexplayer.FlexPlayerView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //    String TEST_PATH = "https://scb.liaidi.com//data//video//2017//12//20171214235251279358.mp4";
//    String TEST_PATH = "https://scb.liaidi.com//data//video//2017//12//20171214235251279358.mp4";
    String TEST_PATH = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
    String TEST_PATH2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testmp4.mp4";
    String TEST_PATH3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apeng/test.mp4";
    String TEST_PATH4 = "https://dakaimg.ciweilive.com/erp_114C13721DB0A5B4D8DFE7AB71A48026.mp4";
    String TEST_PATH5 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/document/video:14";
    String TEST_PATH6 = "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super";

    @Override
    public void onBackPressed() {
        if (!flexPlayerView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    FlexPlayerView flexPlayerView;
    VideoView videoView;

    /**
     * 检查wifi是否处开连接状态
     *
     * @return
     */
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.video_view);
        flexPlayerView = findViewById(R.id.flex_player);
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

        findViewById(R.id.list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                flexPlayerView.setUp(v.getContext(), TEST_PATH3);
//                videoView.setVideoURI(Uri.parse(TEST_PATH4));
//                videoView.setVideoPath(TEST_PATH5);
//                videoView.start();

                Intent intent = new Intent(MainActivity.this, ListVideoActivity.class);
                startActivity(intent);


            }
        });
        flexPlayerView.getController().showBackImage(true);
        flexPlayerView.setUp(this, TEST_PATH4);
//        flexPlayerView.setUp(this, TEST_PATH);
//        flexPlayerView.start();
//        flexPlayerView.enterFullScreen();
//        flexPlayerView.getController().showBackImage(true);

//        flexPlayerView.setContainerBackground(getResources().getDrawable(R.drawable.white_background));
        Log.d("TAG", "is wificonnect : " + isWifiConnect());

    }
}
