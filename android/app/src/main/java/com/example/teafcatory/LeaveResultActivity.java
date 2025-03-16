package com.example.teafcatory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LeaveResultActivity extends AppCompatActivity {
    ImageView back;
    TextView textone,texttwo,textthree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_result);
        back=findViewById((R.id.back));
        back.setOnClickListener(v -> openResultActivity());
        // Get the response message from the intent
        String responseMessage = getIntent().getStringExtra("response_value");
        // Display the response message in the textone TextView
        textone = findViewById(R.id.textView9);
        texttwo = findViewById(R.id.textView10);
        textthree = findViewById(R.id.textView12);
        if(responseMessage.equals("Unknown Class")){
            textone.setText("Not recognize");
            texttwo.setText("");
            textthree.setText("No treatment available.");
        }else if (responseMessage.equals("Blister Blight")) {
            textone.setText("Blister Blight Disease");
            texttwo.setText("Blister Blight is a fungal disease caused by Exobasidium vexans. It primarily affects young tea leaves, leading to the development of water-soaked spots which eventually form blisters. The disease thrives in humid and cool conditions and can significantly reduce tea yields if not managed.");
            textthree.setText(
                    "1. Prune affected leaves regularly.\n" +
                    "2. Apply copper-based fungicides.\n" +
                    "3. Ensure proper air circulation by spacing plants.\n" +
                    "4. Monitor weather conditions to predict disease outbreaks.");
        }else if (responseMessage.equals("Red rust")) {
            textone.setText("Red rust Diseases");
            texttwo.setText("Red Rust is caused by algae from the genus Cephaleuros. It creates reddish-orange patches on the upper side of leaves, which eventually turn rust-colored. The affected leaves often become brittle and fall off. This disease mainly affects the yield and quality of tea leaves.");
            textthree.setText(
                    "1. Remove and destroy affected leaves.\n" +
                    "2. Apply copper fungicides to prevent the spread.\n" +
                    "3. Ensure good drainage and avoid waterlogging.\n" +
                    "4. Regularly monitor the field to detect early symptoms.");

        }
        else if (responseMessage.equals("Gray blight")) {
            textone.setText("Gray blight Diseases");
            texttwo.setText("Gray Blight is a fungal disease caused by Pestalotiopsis species. It affects tea leaves, creating gray or brown lesions, often surrounded by a yellow halo. Severe infections can cause significant defoliation, impacting both the quantity and quality of the tea harvest.");
            textthree.setText(
                    "1. Apply appropriate fungicides, such as Mancozeb.\n" +
                    "2. Remove infected leaves and plant debris.\n" +
                    "3. Avoid overhead irrigation to reduce leaf wetness.\n" +
                    "4. Improve air circulation by proper pruning.");
        }
        else if (responseMessage.equals("Brown Blight")) {
            textone.setText("Brown Blight Diseases");
            texttwo.setText("Brown Blight is caused by the fungus Colletotrichum camelliae. It forms brown or dark lesions on leaves and stems, particularly during rainy seasons. The disease can weaken tea plants, reducing leaf quality and yield.");
            textthree.setText(
                    "1. Apply fungicides such as Carbendazim.\n" +
                    "2. Remove and destroy infected plant parts.\n" +
                    "3. Maintain proper plant spacing for air circulation.\n" +
                    "4. Ensure balanced fertilization to improve plant resistance.");
        }
        else{
            textone.setText(responseMessage);
            Log.e("abc", "abc: " +responseMessage);
        }



    }
    public void openResultActivity() {
        Intent intent = new Intent(this,LeaveResultActivity.class);
        startActivity(intent);
    }
}