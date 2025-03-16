package com.example.teafcatory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    FrameLayout fram1,fram2;
    ImageView logout;
    private String bulkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bulkId = getIntent().getStringExtra("bulkId");
        setContentView(R.layout.home);
        ImageSlider imageSlider = findViewById(R.id.imageslider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        // Adding images and their respective titles to the slider
        slideModels.add(new SlideModel(R.drawable.sl01, " Upload images of tea leaves", ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.sl2, "  Real-time disease identification", ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.sl03, "  Diagnosis and growth stage tracking", ScaleTypes.FIT));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        fram1=findViewById((R.id.frame1));

        fram1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeaveActivity();
            }
        });
        fram2=findViewById((R.id.frame2));
        fram2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlantActivity();
            }
        });
        logout = findViewById(R.id.back);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Logout user
                        FirebaseAuth.getInstance().signOut();
                        // If the user is signed in with email/password
                        openloginActivity();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dismiss dialog, do nothing
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                // Change the button text color to white
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
            }
        });
    }
    public void openLeaveActivity() {
        Intent intent = new Intent(this,LeaveImageUploadActivity.class);
        intent.putExtra("bulkId", bulkId);
        startActivity(intent);
    }

    public void openPlantActivity() {
        Intent intent = new Intent(this,PlantImageUpoladActivity.class);
        intent.putExtra("bulkId", bulkId);
        startActivity(intent);
    }
    public void openloginActivity() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}