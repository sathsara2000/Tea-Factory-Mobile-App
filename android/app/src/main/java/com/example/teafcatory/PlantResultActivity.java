package com.example.teafcatory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlantResultActivity extends AppCompatActivity {
    ImageView back;
    TextView textone,texttwo,textthree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_result);
        back=findViewById((R.id.back));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeActivity();
            }
        });
        // Get the response message from the intent
        String responseMessage = getIntent().getStringExtra("response_value");
        // Display the response message in the textone TextView
        textone = findViewById(R.id.textView9);
        texttwo = findViewById(R.id.textView10);
        textthree = findViewById(R.id.textView12);
        if(responseMessage.equals("Unknown Class")){
            textone.setText("Not recognize");
            texttwo.setText("Please try again");
            textthree.setText("Please upload clear and valid image ");
        }else if (responseMessage.equals("Best")) {
            textone.setText(" Best Quality");
            texttwo.setText("The tea grading result indicates Best Quality");
            textthree.setText(
                            "1. This grade signifies that the tea meets the highest standards of quality, with exceptional leaf texture, uniformity, and minimal impurities.\n" +
                            "2. Such tea is prized for its superior flavor, aroma, and appearance, making it ideal for premium markets.\n" +
                            "3. This level of grading ensures that the tea represents excellence in production, processing, and selection, offering a rich and satisfying experience to tea enthusiasts.\n"
                            );
        }else if (responseMessage.equals("Poor")) {
            textone.setText("Poor Quality");
            texttwo.setText("The tea grading result indicates Poor Quality");
            textthree.setText(
                            "1. This grade suggests significant deviations from quality standards, such as irregular leaf texture, inconsistent color, or the presence of impurities\n" +
                            "2. Tea graded as poor may lack the desired flavor, aroma, and visual appeal expected in higher-quality batches. \n" +
                            "3.  This result highlights the need for improvement in cultivation, harvesting, or processing techniques. \n"
                            );}
        else if (responseMessage.equals("Poor-Best")) {
            textone.setText("Poor-Best Quality");
            texttwo.setText("The tea grading result indicates Poor and Best both Qualities");
            textthree.setText(
                            "1. This grade signifies an intermediate quality, where the tea shows a mix of both good and poor characteristics in texture, uniformity, and impurities.\n" +
                            "2. While the flavor and aroma might have some positive aspects, inconsistencies may affect the overall experience.\n" +
                            "3. This grading reflects potential for improvement, requiring careful attention during production and processing to achieve better quality.\n"
            );

        }
        else{
            textone.setText("Not recognize");
            texttwo.setText("Please try again");
            textthree.setText("Please upload clear and valid image ");
        }
    }
    public void openHomeActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
}