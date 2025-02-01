package com.hematify;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hematify.fragments.HomeFragment;
import com.hematify.fragments.NotificationFragment;
import com.hematify.fragments.ProfileFragment;

public class Bottom_Navigation extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private LinearLayout dotsContainer;
    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private ImageView[] dots;
    private final Handler handler = new Handler();
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);


        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Default fragment on launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }


            return true;
        });
    }

}
