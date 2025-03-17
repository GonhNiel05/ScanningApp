package com.example.textrecognitionactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

public class ProfileActivity extends AppCompatActivity {
    
    private ShapeableImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private TextView documentsCount;
    private TextView textCount;
    private TextView pdfCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        // Initialize UI components
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        documentsCount = findViewById(R.id.documents_count);
        textCount = findViewById(R.id.text_count);
        pdfCount = findViewById(R.id.pdf_count);
        
        // Load user data
        loadUserData();
        
        // Load stats
        loadStatistics();
        
        // Set up setting options click listeners
        setupSettingsClickListeners();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back when back button in toolbar is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void loadUserData() {
        // In a real app, this would load from account system or local storage
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", "OCR User");
        String email = prefs.getString("user_email", "user@example.com");
        
        profileName.setText(name);
        profileEmail.setText(email);
        
        // Edit profile button click
        findViewById(R.id.edit_profile_button).setOnClickListener(v -> {
            // In a real app, this would open a profile edit screen
            Snackbar.make(v, "Edit profile functionality coming soon", Snackbar.LENGTH_SHORT).show();
        });
    }
    
    private void loadStatistics() {
        // In a real app, these would come from a database
        SharedPreferences statsPrefs = getSharedPreferences("stats_prefs", MODE_PRIVATE);
        int documents = statsPrefs.getInt("documents_scanned", 0);
        int textsRecognized = statsPrefs.getInt("texts_recognized", 0);
        int pdfsCreated = statsPrefs.getInt("pdfs_created", 0);
        
        documentsCount.setText(String.valueOf(documents));
        textCount.setText(String.valueOf(textsRecognized));
        pdfCount.setText(String.valueOf(pdfsCreated));
    }
    
    private void setupSettingsClickListeners() {
        // Language settings
        findViewById(R.id.language_settings).setOnClickListener(v -> {
            // In a real app, this would open language settings
            Snackbar.make(v, "Language settings coming soon", Snackbar.LENGTH_SHORT).show();
        });
        
        // Storage settings
        findViewById(R.id.storage_settings).setOnClickListener(v -> {
            // In a real app, this would open storage management
            Snackbar.make(v, "Storage settings coming soon", Snackbar.LENGTH_SHORT).show();
        });
        
        // Theme settings
        findViewById(R.id.theme_settings).setOnClickListener(v -> {
            // In a real app, this would open theme picker
            Snackbar.make(v, "Theme settings coming soon", Snackbar.LENGTH_SHORT).show();
        });
        
        // About app
        findViewById(R.id.about_app).setOnClickListener(v -> {
            // In a real app, this would open about screen
            Snackbar.make(v, "About app coming soon", Snackbar.LENGTH_SHORT).show();
        });
    }
} 