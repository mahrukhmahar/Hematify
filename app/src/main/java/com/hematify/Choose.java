package com.hematify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Choose extends AppCompatActivity {
    private Button btnNeedBlood, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


        // Initialize Buttons
        btnNeedBlood = findViewById(R.id.btn_need_blood);
        btnRegister = findViewById(R.id.btn_register);

        // Set Click Listeners
        btnNeedBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Bottom_Navigation Activity
                startActivity(new Intent(Choose.this, Bottom_Navigation.class));

                // Use Handler to wait for 2 seconds before launching another activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // After 2 seconds, open the next activity
                        startActivity(new Intent(Choose.this, DonorRequestActivity.class));  // Replace AnotherActivity with your desired activity
                    }
                }, 500);  // 2000 milliseconds = 2 seconds
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Choose.this, Login.class));
            }
        });



    }

}