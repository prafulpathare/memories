package com.cigatee.gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> imagePaths;
    private List<String> selectedImages = new ArrayList<>();
    private RecyclerView photosView;
    private ImageAdapter imageAdapter;

    private int action;

    private String searchText;


    private TypeWriterView greetings;
    private TextView dateAndTimeView;
    private Handler greetingsHandler = new Handler();

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

        requestAllFilesPermission();

        dateAndTimeView = findViewById(R.id.dnt);
        Date date = Date.from(Instant.now());
        dateAndTimeView.setText(String.format("%s %s %s %s:%s", date.getDate(), Month.of(date.getMonth()), Year.now(), date.getHours(), date.getMinutes()));

        greetings = findViewById(R.id.greetings);
        greetings.setCharacterDelay(40); // 80ms delay between characters
        greetings.animateText(CommonUtility.getGreetings());

        greetingsHandler.postDelayed(() -> greetings.animateText("explore your memories"), 5000);
        greetingsHandler.postDelayed(() -> {
            greetings.setVisibility(View.GONE);
        }, 20000);


        photosView = findViewById(R.id.photosView);
        photosView.setLayoutManager(new GridLayoutManager(this, 2));
//        photosView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        photoSearch = findViewById(R.id.photoSearch);
        photoSearch.addTextChangedListener(onPhotoSearch());
        photoSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (action == R.drawable.ic_search)
                searchPhoto();
            else if (action == R.drawable.ic_edit)
                renameSelectedFiles();


            return true;
        });

        action = R.drawable.ic_search;

        foldersList = findViewById(R.id.folders);
        foldersAdapter = new HorizontalStringAdapter(this, new ArrayList<>(), selectedFolders, folderName -> {
            if (selectedFolders.contains(folderName)) selectedFolders.remove(folderName);
            else selectedFolders.add(folderName);
            searchPhoto();
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

        if(imagePaths == null) imagePaths = new ArrayList<>();
        if(folders == null) folders = new HashSet<>();

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

        imageAdapter = new ImageAdapter(
                this,
                imagePaths,
                selectedImages,
                path -> {
                    if (!selectedImages.contains(path))
                        selectedImages.add(path);
                    else
                        selectedImages.remove(path);


                    if (selectedImages.isEmpty()) {
                        action = R.drawable.ic_search;
                    } else {
                        action = R.drawable.ic_edit;
                    }

                    reloadSearchBar();
                }
        );

        photosView.setAdapter(imageAdapter);

        foldersAdapter.setFolders(new ArrayList<>(folders));
        foldersAdapter.populateLinearLayout(foldersList);
    }

    void reloadSearchBar() {

        searchText = "";
        photoSearch.setText(searchText);
        photoSearch.setCompoundDrawablesWithIntrinsicBounds(action, 0, 0, 0);


    }

    void searchPhoto() {

        if (searchText == null) return;

        imageAdapter.setImagePaths(
                imagePaths.stream().filter(img -> {
                    boolean showImage = selectedFolders.isEmpty() && img.contains(searchText);

                    if (!selectedFolders.isEmpty() && selectedFolders.stream().anyMatch(folder -> img.contains(folder) && img.contains(searchText)))
                        showImage = true;

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
                searchText = s.toString();

                if (selectedImages.isEmpty()) searchPhoto();
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

    void renameSelectedFiles() {

        int id = 1;

        for (String image : selectedImages) {
            try {

                File oldFile = new File(image);
                if (!oldFile.exists()) continue;

                String extension = image.substring(image.lastIndexOf("."));
                File newFile = new File(oldFile.getParent(), String.format("%s_%s_%s", searchText, id++, extension));
                oldFile.renameTo(newFile);
            } catch (Exception e) {
                dateAndTimeView.setText(e.getMessage());
            }
        }

        selectedImages.clear();

        action = R.drawable.ic_search;
        photoSearch.setText("");

        loadImages();
        reloadSearchBar();
    }

    private void requestAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent); // user must toggle "Allow management of all files"
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            } else {
                // already granted
            }
        }
    }

}
