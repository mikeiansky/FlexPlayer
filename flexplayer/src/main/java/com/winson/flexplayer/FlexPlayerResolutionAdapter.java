package com.winson.flexplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @date on 2019/1/9
 * @Author Winson
 */
class FlexPlayerResolutionAdapter extends RecyclerView.Adapter {

    interface OnItemClickListener {
        void onItemClick(View child, int position, FlexPlayerResolution resolution);
    }

    static class FlexPlayerResolutionViewHolder extends RecyclerView.ViewHolder {

        FlexPlayerResolutionViewHolder(@NonNull View itemView) {
            super(itemView);
        }

    }

    private List<FlexPlayerResolution> resolutions;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(List<FlexPlayerResolution> resolutions) {
        this.resolutions = resolutions;
        notifyDataSetChanged();
    }

    public boolean haveResolution() {
        return resolutions != null && resolutions.size() > 0;
    }

    @Override
    public int getItemCount() {
        if (resolutions == null) {
            return 0;
        } else {
            return resolutions.size();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.flex_player_resolution_item, parent, false);
        return new FlexPlayerResolutionViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final FlexPlayerResolution resolution = resolutions.get(position);
        TextView titleTextView = holder.itemView.findViewById(R.id.title);
        titleTextView.setText(resolution.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position, resolution);
                }
            }
        });
    }

}
