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

    interface OnItemClickListener {
        void onItemClick(View child, int position, FlexPlayerSpeed speed);
    }

    private static class FlexPlayerSpeedViewHolder extends RecyclerView.ViewHolder {

        public FlexPlayerSpeedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private float speed = 1.0f;
    private OnItemClickListener onItemClickListener;

    public void updateSpeed(float speed) {
        this.speed = speed;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.flex_player_speed_item, parent, false);
        return new FlexPlayerSpeedViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final FlexPlayerSpeed speedItem = FlexPlayerSpeed.values()[position];
        TextView titleTV = holder.itemView.findViewById(R.id.title);
        titleTV.setText(speedItem.getName());
        if (speed == speedItem.getSpeed()) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speed != speedItem.getSpeed()) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, position, speedItem);
                    }
                    updateSpeed(speedItem.getSpeed());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return FlexPlayerSpeed.values().length;
    }

}
