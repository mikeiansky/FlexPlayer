package com.winson.flexplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @date on 2019/1/10
 * @Author Winson
 */
class FlexPlayerSpeedAdapter extends RecyclerView.Adapter {

    private static class FlexPlayerSpeedViewHolder extends RecyclerView.ViewHolder {

        public FlexPlayerSpeedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.flex_player_speed_item, parent, false);
        return new FlexPlayerSpeedViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FlexPlayerSpeed speed = FlexPlayerSpeed.values()[position];
        TextView titleTV = holder.itemView.findViewById(R.id.title);
        titleTV.setText(speed.getName());
    }

    @Override
    public int getItemCount() {
        return FlexPlayerSpeed.values().length;
    }

}
