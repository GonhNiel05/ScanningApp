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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.text.method.ScrollingMovementMethod;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
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

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ImageView cameraImage;
    private Button captureImgBtn, uploadImageBtn, copyTextBtn, clearTextBtn, convertTextBtn;
    private TextView resultText;
    private ImageButton historyBtn;
    private String currentPhotoPath = null;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    //lắng nghe click để pick ảnh

    // Activity result launchers for permission and taking pictures
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views and event handlers
        initializeViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    private void initializeViews() {
        // Linking UI components with their XML counterparts
        cameraImage = findViewById(R.id.cameraImage);
        captureImgBtn = findViewById(R.id.captureImgBtn);
        uploadImageBtn = findViewById(R.id.uploadImgBtn);
        copyTextBtn = findViewById(R.id.copyTextBtn);
        clearTextBtn = findViewById(R.id.clearTextBtn);
        convertTextBtn = findViewById(R.id.convertTextBtn);
        resultText = findViewById(R.id.resultText);
        historyBtn = findViewById(R.id.historyBtn);

        resultText.setMovementMethod(new ScrollingMovementMethod()); // Enable scrolling for text view
        copyTextBtn.setVisibility(Button.GONE); // Hide copy button initially

        // Request permission to use camera
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            captureImage();
                        } else {
                            Toast.makeText(MainActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // Khi nhấn nút "Lịch sử", mở `HistoryActivity`
        historyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });


        // Take picture and handle result
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean success) {
                        if (success && currentPhotoPath != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                            cameraImage.setImageBitmap(bitmap);
                            recognizeText(bitmap);
                        }
                    }
                }
        );

        // Handle capture button click
        captureImgBtn.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.CAMERA));


        //Handle pick image click
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Chuyển URI thành Bitmap
                                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                cameraImage.setImageBitmap(bitmap); // Hiển thị ảnh
                                recognizeText(bitmap); // Nhận diện văn bản từ ảnh
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );


        uploadImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*"); // Chỉ chọn ảnh
            pickImageLauncher.launch(intent);
        });


        //nút Clear
        // Xử lý sự kiện khi nhấn nút Clear
        clearTextBtn.setOnClickListener(v -> {
            resultText.setText(""); // Xóa nội dung trong TextView
            cameraImage.setImageDrawable(null); // Xóa ảnh trong ImageView
        });

        convertTextBtn.setOnClickListener(v -> {
            try {
                convertTextToPDF();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
            Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
            takePictureLauncher.launch(photoUri);
        }
    }

    private void recognizeText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Bộ nhận diện chữ Latin
        TextRecognizer latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Bộ nhận diện chữ Trung Quốc
        TextRecognizer chineseRecognizer = TextRecognition.getClient(
                new com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions.Builder().build()
        );

        latinRecognizer.process(image)
                .addOnSuccessListener(latinText -> {
                    String latinResult = latinText.getText();

                    chineseRecognizer.process(image)
                            .addOnSuccessListener(chineseText -> {
                                String chineseResult = chineseText.getText();
                                String finalResult = latinResult + "\n" + chineseResult;

                                resultText.setText(finalResult);
                                copyTextBtn.setVisibility(Button.VISIBLE);

                                // Lưu vào lịch sử
                                saveToHistory(finalResult);

                                copyTextBtn.setOnClickListener(v -> {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Recognized Text", finalResult);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(MainActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();
                                });

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(MainActivity.this, "Lỗi nhận diện tiếng Trung: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Lỗi nhận diện tiếng Latin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Hàm lưu văn bản vào lịch sử
    // Hàm lưu văn bản vào lịch sử SharedPreferences
    private void saveToHistory(String text) {
        // Mở SharedPreferences với tên "TextHistory"
        SharedPreferences prefs = getSharedPreferences("TextHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Lấy danh sách lịch sử hiện tại dưới dạng chuỗi JSON
        String historyJson = prefs.getString("history", "[]"); // Nếu chưa có thì dùng mảng rỗng
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(historyJson); // Chuyển chuỗi JSON thành mảng
        } catch (JSONException e) {
            jsonArray = new JSONArray(); // Nếu lỗi, tạo mảng rỗng mới
        }

        // Thêm dữ liệu mới vào danh sách
        jsonArray.put(text);

        // Giới hạn lịch sử tối đa 10 mục: nếu vượt quá thì xóa mục cũ nhất
        if (jsonArray.length() > 10) {
            jsonArray.remove(0); // Xóa phần tử đầu tiên (cũ nhất)
        }

        // Lưu danh sách mới vào SharedPreferences
        editor.putString("history", jsonArray.toString());
        editor.apply(); // Lưu thay đổi
    }


    //Create methods convert Text to PDF
    private void convertTextToPDF() throws IOException {
        String text = resultText.getText().toString(); // Lấy nội dung từ TextView

        if (text.isEmpty()) {
            Toast.makeText(this, "Không có nội dung để xuất PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng PdfDocument
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Vẽ nội dung lên PDF
        int x = 50, y = 50;
        for (String line : text.split("\n")) { // Xử lý xuống dòng
            canvas.drawText(line, x, y, paint);
            y += 20; // Khoảng cách giữa các dòng
        }

        pdfDocument.finishPage(page);

        // Lưu file vào thư mục "Documents"
        File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (!directory.exists()) {
            directory.mkdirs(); // Tạo thư mục nếu chưa có
        }

        File file = new File(directory, "ConvertedText.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.close();

        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);

        Toast.makeText(this, "PDF đã lưu: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }



}
