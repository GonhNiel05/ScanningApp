package com.example.textrecognitionactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay and then check if onboarding is needed
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if onboarding is completed
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean onboardingCompleted = prefs.getBoolean("onboarding_completed", false);
            
            Intent intent;
            if (onboardingCompleted) {
                // Go directly to main activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Show onboarding first
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            }
            
            startActivity(intent);
            finish(); // Close the splash activity
        }, SPLASH_DELAY);
    }
} 