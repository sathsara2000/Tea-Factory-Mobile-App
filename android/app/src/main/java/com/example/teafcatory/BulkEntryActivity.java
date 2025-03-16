package com.example.teafcatory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class BulkEntryActivity extends AppCompatActivity {
    private EditText editDriverName, editLorryNumber;
    private TextView textBulkNumber;
    private Button btnSubmit, btnBulkHistory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private int bulkNumber = 1; // Default bulk number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_entry);

        editDriverName = findViewById(R.id.editDriverName);
        editLorryNumber = findViewById(R.id.editLorryNumber);
        textBulkNumber = findViewById(R.id.textBulkNumber);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBulkHistory = findViewById(R.id.btnBulkHistory);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            fetchLatestBulkNumber();
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirestore();
            }

        });

        btnBulkHistory.setOnClickListener(v -> {
            Intent intent = new Intent(BulkEntryActivity.this, BulkHistoryActivity.class);
            startActivity(intent);
        });
    }

    // Fetch the latest bulk number from Firestore
    private void fetchLatestBulkNumber() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("bulkData")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (!snapshots.isEmpty()) {
                            bulkNumber = snapshots.size() + 1;
                        }
                        textBulkNumber.setText("Bulk Number: " + bulkNumber);
                    }
                });
    }

    // Save form data to Firestore
    private void saveDataToFirestore() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverName = editDriverName.getText().toString().trim();
        String lorryNumber = editLorryNumber.getText().toString().trim();

        Map<String, Object> bulkEntry = new HashMap<>();
        bulkEntry.put("driverName", driverName.isEmpty() ? "N/A" : driverName);
        bulkEntry.put("lorryNumber", lorryNumber.isEmpty() ? "N/A" : lorryNumber);
        bulkEntry.put("bulkNumber", bulkNumber);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("bulkData")
                .add(bulkEntry)
                .addOnSuccessListener(documentReference -> {
                    String bulkId = documentReference.getId();
                    Toast.makeText(BulkEntryActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BulkEntryActivity.this, HomeActivity.class);
                    intent.putExtra("bulkId", bulkId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BulkEntryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
