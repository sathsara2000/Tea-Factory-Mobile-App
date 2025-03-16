package com.example.teafcatory;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantImageUpoladActivity extends AppCompatActivity {
    ImageView back;
    private ImageView imageView;
    private Button uploadButton;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;
    private static final int REQUEST_IMAGE_CROP = 3;

    private FirebaseFirestore firestore;
    private String userId,bulkId;
    private StorageReference storageReference;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    // Use the cropped image URI.
                    Uri croppedImageUri = result.getUriContent();
                    imageView.setImageURI(croppedImageUri);
                    imageUri = croppedImageUri;
                } else {
                    // Handle the error.
                    Exception exception = result.getError();
                    Toast.makeText(this, "Crop error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_image_upolad);
        back=findViewById((R.id.back));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeActivity();
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait for image detection...");
        progressDialog.setCancelable(false);

        imageView = findViewById(R.id.imageView);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        bulkId = getIntent().getStringExtra("bulkId");

        findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        findViewById(R.id.cam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });

        uploadButton = findViewById(R.id.buttonupload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }
    public void openHomeActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }
    private void uploadImage() {
        if (imageUri != null) {
            progressDialog.show();
            StorageReference fileRef = storageReference.child("images/" + userId + "/" + bulkId + "/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();  // Firebase image URL

                        // Pass the correct Firebase image URL
                        new PlantImageUpoladActivity.UploadImageTask(imageUrl).execute(imageUri.toString());

                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(PlantImageUpoladActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), "No image selected", Snackbar.LENGTH_SHORT).show();
        }
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);

        } else {
            // Permission has already been granted
            dispatchTakePictureIntent();
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.teafcatory", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                imageUri = photoURI;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void saveImageResult(String imageUrl, String predictionResult) {

        // Reference to the user's bulkData document to get the bulkNumber
        DocumentReference bulkDataRef = firestore.collection("users").document(userId)
                .collection("bulkData").document(bulkId);

        bulkDataRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch the bulkNumber from the bulkData document
                Long bulkNumber = documentSnapshot.getLong("bulkNumber");
                System.out.println("bulkNumber: " + bulkNumber);
                if (bulkNumber != null) {
                    // Reference to the user's leavePrediction with bulkNumber
                    DocumentReference leavePredictionRef = firestore.collection("users").document(userId)
                            .collection("plantPrediction").document(String.valueOf(bulkNumber));

                    // Prepare new prediction data
                    Map<String, Object> newPredictionData = new HashMap<>();
                    newPredictionData.put("imageUrl", imageUrl);
                    newPredictionData.put("predictionResult", predictionResult);
                    newPredictionData.put("timestamp", new Date());

                    // Check if the leavePrediction document exists
                    leavePredictionRef.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            // Document exists, append new prediction to the array
                            leavePredictionRef.update("predictions", FieldValue.arrayUnion(newPredictionData))
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Image and prediction updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Failed to update image and prediction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Document doesn't exist, create it with the first prediction
                            Map<String, Object> newDocumentData = new HashMap<>();
                            List<Map<String, Object>> predictionsList = new ArrayList<>();
                            predictionsList.add(newPredictionData);
                            newDocumentData.put("predictions", predictionsList);

                            leavePredictionRef.set(newDocumentData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Image and prediction saved successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Failed to save image and prediction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to check leavePrediction document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Bulk number not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Bulk data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to fetch bulk number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                dispatchTakePictureIntent();
            } else {
                // Camera permission denied
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                startCrop(imageUri);  // Start cropping the captured image
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                startCrop(imageUri);  // Start cropping the selected image
            }
        }
    }
    // Function to start the crop activity
    private void startCrop(Uri uri) {
        CropImageContractOptions options = new CropImageContractOptions(uri, new CropImageOptions());
        cropImage.launch(options);
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    private class UploadImageTask extends AsyncTask<String, Void, String> {
        private String imageUrl; // Firebase image URL

        public UploadImageTask(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        @Override
        protected String doInBackground(String... strings) {
            String imageUrl = strings[0];
            String responseMessage = "";

            try {
                // Get a file from the imageUri
                Uri imageUri = Uri.parse(imageUrl);

                // Convert URI to file (may need to copy content:// Uri to a temp file first)
                File file = createFileFromUri(imageUri);

                if (file == null) {
                    responseMessage = "Error: Could not find file";
                    return responseMessage;
                }

                // Create a HttpURLConnection to the URL endpoint
                URL url = new URL("https://us-central1-tea-factory-448104.cloudfunctions.net/teagrading");
//                URL url = new URL("https://us-central1-tea-factory-management-system.cloudfunctions.net/gradingfinal");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Create a MultipartEntityBuilder to build the request body
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                // Add the image file to the request body
                entityBuilder.addBinaryBody("image", file, ContentType.DEFAULT_BINARY, file.getName());

                HttpEntity entity = entityBuilder.build();
                urlConnection.setRequestProperty("Content-Type", entity.getContentType().getValue());

                // Write the request body to the connection output stream
                try (OutputStream out = urlConnection.getOutputStream()) {
                    entity.writeTo(out);
                }

                // Get the response from the server
                int responseCode = urlConnection.getResponseCode();
                Log.d("UploadImageTask", "Response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    responseMessage = convertStreamToString(inputStream);
                } else {
                    responseMessage = "Error: " + responseCode;
                }
                Log.d("UploadImageTask", "Response message: " + responseMessage);

                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                responseMessage = "Exception: " + e.getMessage();
                Log.e("UploadImageTask", "Error uploading image: " + e.getMessage());
            }

            return responseMessage;
        }

        private File createFileFromUri(Uri uri) throws IOException {
            File tempFile = null;

            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                String fileName = "upload_image.jpg";
                tempFile = new File(getCacheDir(), fileName);

                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return tempFile;
        }

        private String convertStreamToString(InputStream inputStream) {
            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        @Override
        protected void onPostExecute(String responseMessage) {
            Log.e("UploadImageTask", "Response message: " + responseMessage);
            progressDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(responseMessage);

                if (jsonObject.has("error")) {
                    showAlertDialog("Error", "Please upload a valid image");
                } else if (jsonObject.has("class")) {
                    String predictionResult = jsonObject.getString("class");

                    // Check for "Unknown Class"
                    if (predictionResult.equals("Unknown Class")) {
                        showAlertDialog("Error", "Please upload clear image");
                    } else if (predictionResult.equals("[]")) {
                        showAlertDialog("Error", "Invalid image type");
                    }else {
                        saveImageResult(imageUrl, predictionResult);
                        Intent intent = new Intent(PlantImageUpoladActivity.this, PlantResultActivity.class);
                        intent.putExtra("response_value", predictionResult);
                        intent.putExtra("imageUri", imageUri.toString());
                        startActivity(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage());
            }
        }
    }
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(PlantImageUpoladActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with click action
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}