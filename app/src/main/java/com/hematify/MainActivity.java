//package com.hematify;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.hematify.fragments.HomeFragment;
//
//public class MainActivity extends AppCompatActivity {
//
//
//    private Bottom_Navigation bottomNavigationView;
//    private FrameLayout frameLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        frameLayout = findViewById(R.id.content_frame);
//
//        // Load HomeFragment by default when MainActivity starts
//        loadFragment(new HomeFragment());
//
//        // Set up Bottom Navigation listener
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.nav_home:
//                    loadFragment(new HomeFragment());
//                    return true;
//                case R.id.nav_search:
//                    loadFragment(new SearchFragment());
//                    return true;
//                case R.id.nav_profile:
//                    loadFragment(new ProfileFragment());
//                    return true;
//                default:
//                    return false;
//            }
//        });
//    }
//
//    // Method to load fragments
//    private void loadFragment(Fragment fragment) {
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.content_frame, fragment); // Replace the fragment in the container
//        transaction.commit(); // Commit the transaction
//    }
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
//        setContentView(R.layout.activity_main);
//
//        int SPLASH_SCREEN = 3000;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent=new Intent(MainActivity.this,Choose.class);
//                startActivity(intent);
//                finish();
//            }
//        }, SPLASH_SCREEN);
//
//    }
//}