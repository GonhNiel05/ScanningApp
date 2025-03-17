package com.example.textrecognitionactivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView emptyHistoryText;
    private TextView documentsCount;
    private TextView textCount;
    private TextView pdfCount;
    private List<HistoryAdapter.HistoryItem> historyItems;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyHistoryText = findViewById(R.id.emptyHistoryText);
        documentsCount = findViewById(R.id.dashboard_documents_count);
        textCount = findViewById(R.id.dashboard_text_count);
        pdfCount = findViewById(R.id.dashboard_pdf_count);
        
        // Load dashboard statistics
        loadDashboardStats();
        
        // Set up RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyItems = loadHistory();
        adapter = new HistoryAdapter(this, historyItems);
        historyRecyclerView.setAdapter(adapter);
        
        // Show empty view if no history
        if (historyItems.isEmpty()) {
            emptyHistoryText.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            emptyHistoryText.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadDashboardStats() {
        SharedPreferences statsPrefs = getSharedPreferences("stats_prefs", MODE_PRIVATE);
        int documents = statsPrefs.getInt("documents_scanned", 0);
        int textsRecognized = statsPrefs.getInt("texts_recognized", 0);
        int pdfsCreated = statsPrefs.getInt("pdfs_created", 0);
        
        documentsCount.setText(String.valueOf(documents));
        textCount.setText(String.valueOf(textsRecognized));
        pdfCount.setText(String.valueOf(pdfsCreated));
    }

    private List<HistoryAdapter.HistoryItem> loadHistory() {
        SharedPreferences prefs = getSharedPreferences("TextHistory", MODE_PRIVATE);
        String historyJson = prefs.getString("history", "[]");

        List<HistoryAdapter.HistoryItem> items = new ArrayList<>();
        try {
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                JSONArray entry = historyArray.getJSONArray(i);
                String timestamp = entry.getString(0);
                String text = entry.getString(1);
                items.add(new HistoryAdapter.HistoryItem(timestamp, text));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }
}
