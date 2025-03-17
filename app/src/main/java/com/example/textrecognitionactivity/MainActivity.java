package com.example.textrecognitionactivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private FloatingActionButton cameraFab;
    private BottomNavigationView bottomNavView;
    
    // Feature grid elements
    private LinearLayout pdfToolFeature;
    private LinearLayout idCardFeature;
    private LinearLayout extractTextFeature;
    private LinearLayout captureImageFeature;
    private LinearLayout aiSolverFeature;
    private LinearLayout importFileFeature;
    private LinearLayout imageRecoveryFeature;
    private LinearLayout viewAllFeature;
    
    // Recent items
    private LinearLayout emptyRecentState;
    private RecyclerView recentItemsRecyclerView;
    private TextView viewAllRecentBtn;
    
    private String currentPhotoPath = null;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views and event handlers
        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        setupFeatureGrid();
        setupLaunchers();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupBottomNavigation() {
        bottomNavView = findViewById(R.id.bottomNav);
        
        // Set up listener for bottom navigation
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.navigation_home) {
                // Already on home screen
                return true;
            } else if (id == R.id.navigation_files) {
                openHistoryActivity();
                return true;
            } else if (id == R.id.navigation_capture) {
                requestCameraPermission();
                return false; // Don't select this item
            } else if (id == R.id.navigation_tools) {
                showSnackbar("Tools section clicked");
                return true;
            } else if (id == R.id.navigation_profile) {
                openProfileActivity();
                return true;
            }
            
            return false;
        });
    }
    
    private void setupFeatureGrid() {
        // Find feature elements
        pdfToolFeature = findViewById(R.id.pdfToolFeature);
        extractTextFeature = findViewById(R.id.extractTextFeature);
        captureImageFeature = findViewById(R.id.captureImageFeature);
        importFileFeature = findViewById(R.id.importFileFeature);
        imageRecoveryFeature = findViewById(R.id.imageRecoveryFeature);
        viewAllFeature = findViewById(R.id.viewAllFeature);
        
        // Hide removed features
        if (idCardFeature != null) {
            idCardFeature.setVisibility(View.GONE);
        }
        
        if (aiSolverFeature != null) {
            aiSolverFeature.setVisibility(View.GONE);
        }
        
        // Set click listeners
        pdfToolFeature.setOnClickListener(v -> showSnackbar("PDF Tools selected"));
        extractTextFeature.setOnClickListener(v -> openTextRecognitionScreen());
        captureImageFeature.setOnClickListener(v -> requestCameraPermission());
        importFileFeature.setOnClickListener(v -> pickImageFromGallery());
        imageRecoveryFeature.setOnClickListener(v -> showSnackbar("Image Recovery selected (Premium Feature)"));
        viewAllFeature.setOnClickListener(v -> showSnackbar("View All Features selected"));
    }
    
    private void setupLaunchers() {
        // Request permission to use camera
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        captureImage();
                    } else {
                        showSnackbar(getString(R.string.camera_permission_denied));
                    }
                }
        );

        // Take picture and handle result
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoPath != null) {
                        processCapturedImage();
                    }
                }
        );
        
        // Handle pick image from gallery
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Convert URI to Bitmap
                                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                processImage(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                showSnackbar(getString(R.string.error_processing));
                            }
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Handle settings action
            showSnackbar("Settings clicked");
            return true;
        } else if (id == R.id.action_help) {
            // Handle help action
            showSnackbar("Help clicked");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        // Main views
        cameraFab = findViewById(R.id.cameraFab);
        
        // Recent views
        emptyRecentState = findViewById(R.id.emptyRecentState);
        recentItemsRecyclerView = findViewById(R.id.recentItemsRecyclerView);
        viewAllRecentBtn = findViewById(R.id.viewAllRecentBtn);
        
        // Setup empty state for recent items
        emptyRecentState.setVisibility(View.VISIBLE);
        recentItemsRecyclerView.setVisibility(View.GONE);
        
        // Set click listeners for main action buttons
        cameraFab.setOnClickListener(v -> requestCameraPermission());
        viewAllRecentBtn.setOnClickListener(v -> openHistoryActivity());
    }
    
    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
    
    private void openHistoryActivity() {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }
    
    private void openProfileActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
    
    private void openTextRecognitionScreen() {
        showSnackbar("Opening text recognition");
        // For demonstration, we'll use the camera action
        requestCameraPermission();
    }
    
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // Show a snackbar message
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    // Create an image file for storing captured photos
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Capture an image and store it
    private void captureImage() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            showSnackbar("Error creating the file");
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
            takePictureLauncher.launch(photoUri);
        }
    }
    
    private void processCapturedImage() {
        if (currentPhotoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            processImage(bitmap);
        }
    }
    
    private void processImage(Bitmap bitmap) {
        // Open TextRecognitionActivity with this image
        Intent intent = new Intent(this, TextRecognitionActivity.class);
        // Save bitmap to a temporary file and pass the path
        File tempFile = null;
        try {
            tempFile = saveBitmapToTemp(bitmap);
            intent.putExtra("imagePath", tempFile.getAbsolutePath());
            startActivity(intent);
        } catch (IOException e) {
            showSnackbar("Error processing image");
            e.printStackTrace();
        }
    }
    
    private File saveBitmapToTemp(Bitmap bitmap) throws IOException {
        File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());
        FileOutputStream out = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();
        return tempFile;
    }
}
