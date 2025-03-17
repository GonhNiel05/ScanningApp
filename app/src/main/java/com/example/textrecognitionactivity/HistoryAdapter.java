package com.example.textrecognitionactivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> historyItems;
    private final Context context;

    public HistoryAdapter(Context context, List<HistoryItem> historyItems) {
        this.context = context;
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.timestampText.setText(item.getTimestamp());
        holder.contentText.setText(item.getText());

        // Copy button click
        holder.copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Recognized Text", item.getText());
            clipboard.setPrimaryClip(clip);
            Snackbar.make(v, R.string.text_copied, Snackbar.LENGTH_SHORT).show();
        });

        // Share button click
        holder.shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, item.getText());
            context.startActivity(Intent.createChooser(shareIntent, "Share text via"));
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView timestampText;
        TextView contentText;
        MaterialButton copyButton;
        MaterialButton shareButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampText = itemView.findViewById(R.id.historyTimestamp);
            contentText = itemView.findViewById(R.id.historyText);
            copyButton = itemView.findViewById(R.id.copyButton);
            shareButton = itemView.findViewById(R.id.shareButton);
        }
    }

    // Model class for history items
    public static class HistoryItem {
        private final String timestamp;
        private final String text;

        public HistoryItem(String timestamp, String text) {
            this.timestamp = timestamp;
            this.text = text;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getText() {
            return text;
        }
    }
} 