package com.cigatee.gallery;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class Pager extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<String> imagePaths;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        viewPager = findViewById(R.id.viewPager);

        // Get data from Intent
        imagePaths = getIntent().getStringArrayListExtra("images");
        currentPosition = getIntent().getIntExtra("position", 0);

        PagerAdapter adapter = new PagerAdapter(this, imagePaths);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }
}
