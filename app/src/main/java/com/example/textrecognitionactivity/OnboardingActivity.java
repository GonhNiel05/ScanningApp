package com.example.textrecognitionactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;
    
    private List<OnboardingItem> onboardingItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        
        viewPager = findViewById(R.id.onboarding_viewpager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
        
        setupOnboardingItems();
        
        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(onboardingAdapter);
        
        setupDots(0);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDots(position);
                
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == onboardingItems.size() - 1) {
                // Last page, finish onboarding
                finishOnboarding();
            } else {
                // Go to next page
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
        
        btnSkip.setOnClickListener(v -> finishOnboarding());
    }
    
    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();
        
        OnboardingItem item1 = new OnboardingItem();
        item1.setTitle("OCR Text Recognition");
        item1.setDescription("Quickly extract text from images with our powerful OCR technology");
        item1.setImageResId(R.drawable.ic_baseline_camera_24);
        
        OnboardingItem item2 = new OnboardingItem();
        item2.setTitle("Organize & Share");
        item2.setDescription("Save your scanned documents, organize them, and easily share with others");
        item2.setImageResId(R.drawable.ic_baseline_folder_24);
        
        OnboardingItem item3 = new OnboardingItem();
        item3.setTitle("Multi-language Support");
        item3.setDescription("Recognize text in multiple languages including English and Chinese");
        item3.setImageResId(R.drawable.ic_baseline_language_24);
        
        onboardingItems.add(item1);
        onboardingItems.add(item2);
        onboardingItems.add(item3);
    }
    
    private void setupDots(int currentPosition) {
        dotsLayout.removeAllViews();
        
        for (int i = 0; i < onboardingItems.size(); i++) {
            ImageView dot = new ImageView(this);
            
            if (i == currentPosition) {
                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dot, params);
        }
    }
    
    private void finishOnboarding() {
        // Mark onboarding as completed
        SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("onboardingComplete", true);
        editor.apply();
        
        // Navigate to MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    
    // Inner class to represent onboarding items
    public static class OnboardingItem {
        private int imageResId;
        private String title;
        private String description;
        
        public int getImageResId() {
            return imageResId;
        }
        
        public void setImageResId(int imageResId) {
            this.imageResId = imageResId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    // Adapter for ViewPager
    public class OnboardingAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
        
        private List<OnboardingItem> onboardingItems;
        
        public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
            this.onboardingItems = onboardingItems;
        }
        
        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnboardingViewHolder(
                    getLayoutInflater().inflate(R.layout.item_onboarding, parent, false)
            );
        }
        
        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            holder.bind(onboardingItems.get(position));
        }
        
        @Override
        public int getItemCount() {
            return onboardingItems.size();
        }
        
        class OnboardingViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            
            private ImageView imageView;
            private TextView textTitle;
            private TextView textDescription;
            
            public OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_onboarding);
                textTitle = itemView.findViewById(R.id.text_title);
                textDescription = itemView.findViewById(R.id.text_description);
            }
            
            void bind(OnboardingItem item) {
                imageView.setImageResource(item.getImageResId());
                textTitle.setText(item.getTitle());
                textDescription.setText(item.getDescription());
            }
        }
    }
} 