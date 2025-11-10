package com.cigatee.gallery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class HorizontalStringAdapter {

    private Context context;
    private List<String> folders;
    private List<String> selectedFolders;
    private OnItemClickListener listener;

    // Define interface for sending data back to activity
    public interface OnItemClickListener {
        void onItemClick(String folderName);
    }

    // Constructor
    public HorizontalStringAdapter(Context context, List<String> folders, List<String> selectedFolders, OnItemClickListener listener) {
        this.context = context;
        this.folders = folders;
        this.selectedFolders = selectedFolders;
        this.listener = listener;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public void setSelectedFolders(List<String> selectedFolders) {
        this.selectedFolders = selectedFolders;
    }

    // Populate LinearLayout
    public void populateLinearLayout(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(context);
        container.removeAllViews();

        for (String folder : folders) {
            View view = inflater.inflate(R.layout.folder_tag, container, false);
            TextView textView = view.findViewById(R.id.folderId);
            textView.setText(folder);

            // Optional: change color for selected ones
            if (selectedFolders != null && selectedFolders.contains(folder)) {
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundResource(R.drawable.selected_bg);
            } else {
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundResource(R.drawable.search_bg);
            }

            // Click listener to send back selected folder
            view.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(folder);
                }
            });

            container.addView(view);
        }
    }
}
