package com.example.textrecognitionactivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyItem;
    private ArrayList<String> historyList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyItem = findViewById(R.id.historyItem);
        historyList = loadHistory(); // Load danh sách lịch sử

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        historyItem.setAdapter(adapter);

        // Xử lý sự kiện khi nhấn vào một item trong ListView
        historyItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = historyList.get(position);

                // Copy văn bản vào Clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", selectedText);
                clipboard.setPrimaryClip(clip);

                // Hiển thị thông báo
                Toast.makeText(HistoryActivity.this, "Đã sao chép văn bản!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<String> loadHistory() {
        SharedPreferences prefs = getSharedPreferences("TextHistory", MODE_PRIVATE);
        String historyJson = prefs.getString("history", "[]");

        ArrayList<String> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(historyJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
