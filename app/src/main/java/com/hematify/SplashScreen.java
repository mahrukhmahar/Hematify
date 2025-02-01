package com.hematify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_splash_screen);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        int SPLASH_SCREEN=3000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser != null) {
                    Intent intent = new Intent(SplashScreen.this, Bottom_Navigation.class); // Replace with your main page
                    startActivity(intent);
                } else {
                    // If the user is not logged in, go to the Choose screen
                    Intent intent = new Intent(SplashScreen.this, Choose.class); // Replace with your Choose screen
                    startActivity(intent);
                }

                // Close the splash screen activity
                finish();
            }
        }, SPLASH_SCREEN);
    }


}