package com.example.cachecleaner;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    private final List<AppInfo> appList;

    public AppAdapter(List<AppInfo> appList) { this.appList = appList; }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(parent.getContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(32, 24, 32, 24);
        layout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView iconView = new ImageView(parent.getContext());
        iconView.setId(View.generateViewId());
        layout.addView(iconView, new android.widget.LinearLayout.LayoutParams(110, 110));

        android.widget.LinearLayout textBlock = new android.widget.LinearLayout(parent.getContext());
        textBlock.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams blockParams = new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blockParams.setMarginStart(32);

        TextView labelView = new TextView(parent.getContext());
        labelView.setId(View.generateViewId());
        labelView.setTextSize(16f);
        labelView.setTextColor(0xFF222222);
        textBlock.addView(labelView);

        TextView sizeView = new TextView(parent.getContext());
        sizeView.setId(View.generateViewId());
        sizeView.setTextSize(13f);
        sizeView.setTextColor(0xFF666666);
        textBlock.addView(sizeView);

        layout.addView(textBlock, blockParams);

        CheckBox checkBox = new CheckBox(parent.getContext());
        checkBox.setId(View.generateViewId());
        layout.addView(checkBox, new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return new AppViewHolder(layout, iconView.getId(), labelView.getId(), sizeView.getId(), checkBox.getId());
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.txtLabel.setText(app.getLabel());
        holder.txtSize.setText("Cache payload: " + app.getFormattedCacheSize());
        holder.imgIcon.setImageDrawable(app.getIcon());
        
        holder.checkApp.setOnCheckedChangeListener(null);
        holder.checkApp.setChecked(app.isChecked());
        holder.checkApp.setOnCheckedChangeListener((btn, isChecked) -> app.setChecked(isChecked));
    }

    @Override
    public int getItemCount() { return appList.size(); }

    public void updateList(List<AppInfo> newList) {
        this.appList.clear();
        this.appList.addAll(newList);
        notifyDataSetChanged();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtLabel, txtSize;
        CheckBox checkApp;

        public AppViewHolder(@NonNull View itemView, int iconId, int labelId, int sizeId, int checkId) {
            super(itemView);
            imgIcon = itemView.findViewById(iconId);
            txtLabel = itemView.findViewById(labelId);
            txtSize = itemView.findViewById(sizeId);
            checkApp = itemView.findViewById(checkId);
        }
    }
}