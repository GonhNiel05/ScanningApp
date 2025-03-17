package com.example.textrecognitionactivity;

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
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
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
import java.util.Date;
import java.util.Locale;

public class TextRecognitionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView imageView;
    private TextView resultText;
    private ProgressBar progressBar;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialButton convertButton;
    
    private String recognizedText = "";
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);
        
        initializeViews();
        setupToolbar();
        
        // Get image path from intent
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            loadImage(imagePath);
        } else {
            showSnackbar("No image provided");
            finish();
        }
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        progressBar = findViewById(R.id.progressBar);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        convertButton = findViewById(R.id.convertButton);
        
        // Initially hide buttons until text is recognized
        copyButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
        convertButton.setVisibility(View.GONE);
        
        // Set click listeners
        copyButton.setOnClickListener(v -> copyTextToClipboard());
        shareButton.setOnClickListener(v -> shareText());
        convertButton.setOnClickListener(v -> {
            try {
                convertTextToPDF();
            } catch (IOException e) {
                showSnackbar("Error creating PDF: " + e.getMessage());
            }
        });
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Text Recognition");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            imageBitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(imageBitmap);
            recognizeText(imageBitmap);
        } else {
            showSnackbar("Image file not found");
            finish();
        }
    }
    
    private void recognizeText(Bitmap bitmap) {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        resultText.setText("Processing image...");
        
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Latin text recognizer
        TextRecognizer latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Chinese text recognizer
        TextRecognizer chineseRecognizer = TextRecognition.getClient(
                new com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions.Builder().build()
        );

        latinRecognizer.process(image)
                .addOnSuccessListener(latinText -> {
                    String latinResult = latinText.getText();

                    chineseRecognizer.process(image)
                            .addOnSuccessListener(chineseText -> {
                                String chineseResult = chineseText.getText();
                                recognizedText = latinResult;
                                
                                if (!chineseResult.isEmpty()) {
                                    recognizedText += "\n\n" + chineseResult;
                                }

                                // Hide progress
                                progressBar.setVisibility(View.GONE);
                                
                                // Show the recognized text
                                resultText.setText(recognizedText);
                                
                                // Show action buttons
                                copyButton.setVisibility(View.VISIBLE);
                                shareButton.setVisibility(View.VISIBLE);
                                convertButton.setVisibility(View.VISIBLE);

                                // Save to history
                                saveToHistory(recognizedText);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                showSnackbar("Chinese recognition error: " + e.getMessage());
                                
                                // Still show Latin result
                                recognizedText = latinResult;
                                resultText.setText(recognizedText);
                                
                                // Show action buttons
                                if (!recognizedText.isEmpty()) {
                                    copyButton.setVisibility(View.VISIBLE);
                                    shareButton.setVisibility(View.VISIBLE);
                                    convertButton.setVisibility(View.VISIBLE);
                                    
                                    // Save to history
                                    saveToHistory(recognizedText);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showSnackbar("Text recognition error: " + e.getMessage());
                });
    }
    
    private void copyTextToClipboard() {
        if (recognizedText.isEmpty()) {
            showSnackbar("No text to copy");
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Recognized Text", recognizedText);
        clipboard.setPrimaryClip(clip);
        showSnackbar("Text copied to clipboard");
    }
    
    private void shareText() {
        if (recognizedText.isEmpty()) {
            showSnackbar("No text to share");
            return;
        }
        
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, recognizedText);
        shareIntent.setType("text/plain");
        
        Intent chooser = Intent.createChooser(shareIntent, "Share text via");
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            showSnackbar("No app available to share text");
        }
    }
    
    // Save text to history in SharedPreferences
    private void saveToHistory(String text) {
        // Open SharedPreferences named "TextHistory"
        SharedPreferences prefs = getSharedPreferences("TextHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Get existing history or create new one
        String historyJson = prefs.getString("history", "[]");
        try {
            JSONArray historyArray = new JSONArray(historyJson);
            
            // Create new entry with timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            JSONArray newEntry = new JSONArray();
            newEntry.put(timestamp);
            newEntry.put(text);
            
            // Add to history (at the beginning)
            historyArray.put(0, newEntry);
            
            // Limit history to 20 entries
            JSONArray limitedArray = new JSONArray();
            int limit = Math.min(historyArray.length(), 20);
            for (int i = 0; i < limit; i++) {
                limitedArray.put(historyArray.get(i));
            }
            
            // Save updated history
            editor.putString("history", limitedArray.toString());
            editor.apply();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    // Convert recognized text to PDF
    private void convertTextToPDF() throws IOException {
        if (recognizedText.isEmpty()) {
            showSnackbar("No text to convert");
            return;
        }

        // Create a new document
        PdfDocument document = new PdfDocument();
        
        // Create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        
        // Start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        // Draw text on the page
        Paint paint = new Paint();
        paint.setTextSize(12);
        
        // Split text into lines
        String[] lines = recognizedText.split("\n");
        float y = 50;
        for (String line : lines) {
            canvas.drawText(line, 50, y, paint);
            y += paint.descent() - paint.ascent();
        }
        
        // Finish the page
        document.finishPage(page);
        
        // Create PDF file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "OCR_" + timeStamp + ".pdf";
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        
        // Write to file
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();
        
        // Show success message with option to view PDF
        Snackbar.make(findViewById(android.R.id.content), "PDF saved: " + fileName, Snackbar.LENGTH_LONG)
                .setAction("View", v -> {
                    // Open PDF with external app
                    Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    // Check if there's an app to handle this
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        showSnackbar("No PDF viewer app found");
                    }
                })
                .show();
    }
    
    // Show a snackbar message
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
} 