package com.cigatee.gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> imagePaths;
    private RecyclerView photosView;
    private ImageAdapter imageAdapter;


    private TextView greetings;
    private LinearLayout foldersList;
    private List<String> selectedFolders = new ArrayList<>();
    private EditText photoSearch;
    private HorizontalStringAdapter foldersAdapter;

    private Set<String> folders;

    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        greetings = findViewById(R.id.greetings);
        greetings.setText(CommonUtility.getGreetings());

        photosView = findViewById(R.id.photosView);
        photosView.setLayoutManager(new GridLayoutManager(this, 2));

        photoSearch = findViewById(R.id.photoSearch);
        photoSearch.addTextChangedListener(onPhotoSearch());

        foldersList = findViewById(R.id.folders);
        foldersAdapter = new HorizontalStringAdapter(this, new ArrayList<>(), selectedFolders, folderName -> {
            if (selectedFolders.contains(folderName)) selectedFolders.remove(folderName);
            else selectedFolders.add(folderName);
            searchPhoto("");
        });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        loadImages();
                    }
                });

        checkAndRequestPermission();

        runAsync();
    }

    private void checkAndRequestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadImages();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void loadImages() {

        imagePaths = new ArrayList<>();
        folders = new HashSet<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                collection,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );

        if (cursor != null) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            while (cursor.moveToNext()) {
                String path = cursor.getString(dataColumn);
                folders.add(CommonUtility.getDestinationFolder(path));
                imagePaths.add(path);
            }
            cursor.close();
        }

        imageAdapter = new ImageAdapter(this, imagePaths);
        photosView.setAdapter(imageAdapter);

        foldersAdapter.setFolders(new ArrayList<>(folders));
        foldersAdapter.populateLinearLayout(foldersList);
    }

    void searchPhoto(String q) {

        if (q == null) return;

        imageAdapter.setImagePaths(
                imagePaths.stream().filter(img -> {
                    boolean showImage = selectedFolders.isEmpty() && img.contains(q);

                    if(!selectedFolders.isEmpty() && selectedFolders.stream().anyMatch(folder -> img.contains(folder) && img.contains(q))) showImage = true;

                    return showImage;
                }).collect(Collectors.toList())
        );

        imageAdapter.notifyDataSetChanged();
        foldersAdapter.populateLinearLayout(foldersList);
    }

    private TextWatcher onPhotoSearch() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPhoto(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private void runAsync() {

//        greetings.postDelayed(() -> {
//            greetings.animate()
//                    .alpha(0f)
//                    .setDuration(500) // fade duration
//                    .withEndAction(() -> greetings.setVisibility(View.GONE))
//                    .start();
//        }, 2000);


    }
}
