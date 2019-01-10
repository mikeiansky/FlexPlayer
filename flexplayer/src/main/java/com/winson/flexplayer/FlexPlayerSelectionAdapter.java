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
class FlexPlayerSelectionAdapter extends RecyclerView.Adapter {

    private static class FlexPlayerSelectionViewHolder extends RecyclerView.ViewHolder {
        public FlexPlayerSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private List<FlexPlayerSelection> selections;

    public void updateSelections(List<FlexPlayerSelection> selections) {
        this.selections = selections;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (selections == null) {
            return 0;
        } else {
            return selections.size();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.flex_player_selection_item, parent, false);
        return new FlexPlayerSelectionViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FlexPlayerSelection selection = selections.get(position);
        TextView titleTV = holder.itemView.findViewById(R.id.title);
        TextView descriptionTV = holder.itemView.findViewById(R.id.description);
        titleTV.setText(selection.getTitle());
        descriptionTV.setText(selection.getDiscription());
    }


}
