package com.example.teafcatory;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teafcatory.DriverLorryItem;
import com.example.teafcatory.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverLorryAdapter extends RecyclerView.Adapter<DriverLorryAdapter.ViewHolder> {
    private List<DriverLorryItem> driverLorryList;

    public DriverLorryAdapter(List<DriverLorryItem> driverLorryList) {
        this.driverLorryList = driverLorryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_lorry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DriverLorryItem item = driverLorryList.get(position);
        holder.driverNameTextView.setText("Driver Name: " + item.getDriverName());
        holder.lorryNumberTextView.setText("Lorry Number: " + item.getLorryNumber());

        // Hide prediction results initially
        holder.imageView.setVisibility(View.GONE);
        holder.predictionResultTextView.setVisibility(View.GONE);

        holder.leavePredictionButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Fetching Leave Predictions...", Toast.LENGTH_SHORT).show();
            fetchPredictions(v.getContext(), item.getDriverName(), item.getLorryNumber(), "leavePrediction");
        });

        holder.plantPredictionButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Fetching Plant Predictions...", Toast.LENGTH_SHORT).show();
            fetchPredictions(v.getContext(), item.getDriverName(), item.getLorryNumber(), "plantPrediction");
        });
    }

//    private void fetchBulkNumbers(Context context, String driverName, String lorryNumber, ViewHolder holder) {
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        CollectionReference leavePredictionRef = FirebaseFirestore.getInstance()
//                .collection("users").document(userId).collection("leavePrediction");
//
//        leavePredictionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            List<String> bulkNumbers = new ArrayList<>();
//
//            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
//                bulkNumbers.add(doc.getId());  // Bulk numbers (e.g., "1", "2", etc.)
//            }
//
//            if (!bulkNumbers.isEmpty()) {
//                showBulkSelectionDialog(context, driverName, lorryNumber, bulkNumbers, holder);  // ✅ Pass holder here
//            } else {
//                Toast.makeText(context, "No bulk data found for this driver.", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(context, "Failed to fetch bulk numbers.", Toast.LENGTH_SHORT).show();
//        });
//    }

//    private void showBulkSelectionDialog(Context context, String driverName, String lorryNumber, List<String> bulkNumbers, ViewHolder holder) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Select Bulk Number");
//
//        String[] bulkArray = bulkNumbers.toArray(new String[0]);
//        builder.setItems(bulkArray, (dialog, which) -> {
//            String selectedBulk = bulkArray[which];
//            fetchPredictions(context, driverName, lorryNumber, selectedBulk, holder);
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//        builder.show();
//    }

    private void fetchPredictions(Context context, String driverName, String lorryNumber, String predictionType) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference predictionRef = FirebaseFirestore.getInstance()
                .collection("users").document(userId).collection(predictionType);

        predictionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<PredictionItem> predictions = new ArrayList<>();

            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Object data = document.get("predictions");

                if (data instanceof List) { // ✅ Handle ArrayList case
                    List<Map<String, Object>> predictionsList = (List<Map<String, Object>>) data;

                    for (Map<String, Object> predictionData : predictionsList) {
                        String imageUrl = (String) predictionData.get("imageUrl");
                        String predictionResult = (String) predictionData.get("predictionResult");

                        if (imageUrl != null && predictionResult != null) {
                            predictions.add(new PredictionItem(imageUrl, predictionResult));
                        }
                    }
                } else if (data instanceof Map) { // ✅ Handle Map case
                    Map<String, Object> predictionsMap = (Map<String, Object>) data;

                    for (Map.Entry<String, Object> entry : predictionsMap.entrySet()) {
                        Map<String, Object> predictionData = (Map<String, Object>) entry.getValue();
                        String imageUrl = (String) predictionData.get("imageUrl");
                        String predictionResult = (String) predictionData.get("predictionResult");

                        if (imageUrl != null && predictionResult != null) {
                            predictions.add(new PredictionItem(imageUrl, predictionResult));
                        }
                    }
                }
            }

            if (!predictions.isEmpty()) {
                showPredictionsDialog(context, predictions);
            } else {
                Toast.makeText(context, "No predictions found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to fetch predictions.", Toast.LENGTH_SHORT).show();
        });
    }


    private void fetchImagesAndResults(Context context, String userId, String selectedBulk, DocumentSnapshot documentSnapshot, ViewHolder holder) {
        Map<String, Object> predictions = (Map<String, Object>) documentSnapshot.get("predictions");

        if (predictions != null) {
            for (Map.Entry<String, Object> entry : predictions.entrySet()) {
                Map<String, Object> prediction = (Map<String, Object>) entry.getValue();
                String imageUrl = (String) prediction.get("imageUrl");
                String predictionResult = (String) prediction.get("predictionResult");

                // Load image and text, then make them visible
                Picasso.get().load(imageUrl).into(holder.imageView);
                holder.predictionResultTextView.setText(predictionResult);

                holder.imageView.setVisibility(View.VISIBLE); // Show image
                holder.predictionResultTextView.setVisibility(View.VISIBLE); // Show text
            }
        } else {
            Toast.makeText(context, "No predictions available for this bulk.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPredictionsDialog(Context context, String driverName, String lorryNumber, String bulkNumber, List<PredictionItem> predictions) {
    }


    private void showPredictionsDialog(Context context, List<PredictionItem> predictions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Predictions");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        for (PredictionItem item : predictions) {
            ImageView imageView = new ImageView(context);
            Picasso.get().load(item.getImageUrl()).into(imageView);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));

            TextView textView = new TextView(context);
            textView.setText("Result: " + item.getPredictionResult());

            layout.addView(imageView);
            layout.addView(textView);
        }

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public int getItemCount() {
        return driverLorryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView driverNameTextView, lorryNumberTextView, predictionResultTextView;
        Button leavePredictionButton, plantPredictionButton;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driverNameTextView);
            lorryNumberTextView = itemView.findViewById(R.id.lorryNumberTextView);
            leavePredictionButton = itemView.findViewById(R.id.leavePredictionButton);
            plantPredictionButton = itemView.findViewById(R.id.plantPredictionButton);

            // Add these fields
            imageView = itemView.findViewById(R.id.predictionImageView);
            predictionResultTextView = itemView.findViewById(R.id.predictionResultTextView);
        }
    }
}
