package com.demo.flexplayer;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winson.flexplayer.FlexPlayerManager;
import com.winson.flexplayer.FlexPlayerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @date on 2018/12/18
 * @Author Winson
 */
public class ListVideoActivity extends AppCompatActivity {
    public static String TEST_PATH = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
    public static String TEST_PATH2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testmp4.mp4";
    public static String TEST_PATH3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apeng/test.mp4";
    public static String TEST_PATH4 = "https://dakaimg.ciweilive.com/erp_114C13721DB0A5B4D8DFE7AB71A48026.mp4";
    public static String TEST_PATH5 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/document/video:14";
    public static String TEST_PATH6 = "http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super";

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new MyViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            FlexPlayerView playerView = holder.itemView.findViewById(R.id.video_view);
            playerView.setUp(holder.itemView.getContext(), TEST_PATH);
        }

        @Override
        public int getItemCount() {
            return 30;
        }
    }

    @Override
    public void onBackPressed() {
        if (FlexPlayerManager.instance().onBackPressd()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FlexPlayerManager.instance().releaseFlexPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlexPlayerManager.instance().pauseFlexPlayer();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list_video);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
                FlexPlayerView playerView = holder.itemView.findViewById(R.id.video_view);
                if (playerView == FlexPlayerManager.instance().getCurrentFlexPlayer()) {
                    FlexPlayerManager.instance().releaseFlexPlayer();
                }
            }
        });

    }

}
