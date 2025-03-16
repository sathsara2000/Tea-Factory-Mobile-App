package com.example.teafcatory;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BulkHistoryActivity extends AppCompatActivity {

    private RecyclerView driverLorryRecyclerView;
    private DriverLorryAdapter driverLorryAdapter;
    private List<DriverLorryItem> driverLorryList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_history);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        driverLorryRecyclerView = findViewById(R.id.driverLorryRecyclerView);
        driverLorryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch driver and lorry list
        fetchDriverAndLorryInfo();
    }

    private void fetchDriverAndLorryInfo() {
        CollectionReference bulkDataRef = firestore.collection("users").document(userId)
                .collection("bulkData");

        bulkDataRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                driverLorryList.clear(); // Clear existing list

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String driverName = document.getString("driverName");
                    String lorryNumber = document.getString("lorryNumber");

                    if (driverName != null && lorryNumber != null) {
                        driverLorryList.add(new DriverLorryItem(driverName, lorryNumber));
                    }
                }

                // Set adapter with updated list
                driverLorryAdapter = new DriverLorryAdapter(driverLorryList);
                driverLorryRecyclerView.setAdapter(driverLorryAdapter);

            } else {
                Toast.makeText(BulkHistoryActivity.this, "No bulk data found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(BulkHistoryActivity.this, "Failed to fetch driver and lorry data.", Toast.LENGTH_SHORT).show();
        });
    }

}
