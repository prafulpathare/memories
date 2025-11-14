package com.cigatee.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;
import java.util.Random;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imagePaths;

    private static Random random = new Random();

    private OnImageLongPressListener longPressListener;
    private List<String> selectedList;

    // New interface
    public interface OnImageLongPressListener {
        void onImageLongPressed(String path);
    }

    // UPDATED constructor
    public ImageAdapter(Context context, List<String> imagePaths, List<String> selectedList,
                        OnImageLongPressListener longPressListener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.selectedList = selectedList;
        this.longPressListener = longPressListener;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        Glide.with(context)
                .asBitmap()
                .load(new File(imagePath))
                .transform(new CenterCrop(), new RoundedCorners(32))
//                .listener(new RequestListener<Bitmap>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
//                                                Target<Bitmap> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
//                                                   DataSource dataSource, boolean isFirstResource) {
//
//                        holder.imageView.post(() -> {
//                            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
//                            params.height = random.nextBoolean() ? 600 : random.nextBoolean() ? 350 : params.width;
//                            holder.imageView.setLayoutParams(params);
//                        });
//
//                        return false;
//                    }
//                })
                .into(holder.imageView);


        // ✔ Highlight if selected
        holder.itemView.setAlpha(selectedList.contains(imagePath) ? 0.2f : 1f);


        // ✔ Long press
        holder.itemView.setOnLongClickListener(v -> {
            longPressListener.onImageLongPressed(imagePath);
            notifyItemChanged(position);
            return true;
        });


        // ✔ Normal click → open pager
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, Pager.class);
            intent.putStringArrayListExtra("images", new java.util.ArrayList<>(imagePaths));
            intent.putExtra("position", position);
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
