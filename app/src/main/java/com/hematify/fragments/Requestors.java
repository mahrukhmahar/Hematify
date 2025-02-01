package com.hematify.fragments;

import static com.hematify.Register.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hematify.Bottom_Navigation;
import com.hematify.DonorSearchActivity;
import com.hematify.Login;
import com.hematify.R;

import java.util.ArrayList;
import java.util.List;

public class Requestors extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BloodRequestAdapter adapter;
    private List<BloodRequest> requestorList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserProvince, currentUserCity;
    private FirebaseAuth auth;
    private TextView notFoundTextView;
    private String currentUserLocation;  // Variable to store the location
    ProgressBar progressBar;// Declare the TextView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestors);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notFoundTextView = findViewById(R.id.notfound);
        progressBar=findViewById(R.id.progressBar);

        adapter = new BloodRequestAdapter(this, requestorList); // Use your adapter class here
        recyclerView.setAdapter(adapter);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;  // Stop further execution
        }




        // Fetch logged-in user's province & city
        fetchCurrentUserLocation();
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            startActivity(new Intent(Requestors.this, Bottom_Navigation.class));
            finish();
        });

    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
        startActivity(intent);
        finish();
    }

    private void fetchCurrentUserLocation() {
        String userId = auth.getCurrentUser().getUid();  // Get logged-in user ID
        Log.d("FetchUserLocation", "Fetching location for user ID: " + userId);

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullLocation = documentSnapshot.getString("location");  // "city, province"

                        if (fullLocation != null) {
                            currentUserLocation = fullLocation;  // Store location as a single string

                            Log.d("FetchUserLocation", "Fetched location: " + currentUserLocation);

                            // Now fetch requestors using the full location (city, province)
                            fetchRequestors(currentUserLocation);
                        } else {
                            Log.e("FetchUserLocation", "Location is null for user: " + userId);
                        }
                    } else {
                        Log.e("FetchUserLocation", "User document not found.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user location", e));
    }


    private void fetchRequestors(String location) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        Log.d("FetchRequestors", "Fetching requestors for location: " + location);

        db.collection("BloodRequests")
                .whereEqualTo("location", location)  // Match the full location (city, province)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestorList.clear(); // Clear previous data

                    // Loop through the query results
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        BloodRequest request = documentSnapshot.toObject(BloodRequest.class);
                        requestorList.add(request);
                    }

                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();

                    // Manage visibility of not found message
                    if (requestorList.isEmpty()) {
                        notFoundTextView.setVisibility(View.VISIBLE); // Show message
                        recyclerView.setVisibility(View.GONE); // Hide RecyclerView
                    } else {
                        notFoundTextView.setVisibility(View.GONE); // Hide message
                        recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                    }

                    // Hide progress bar after loading data
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching requestors", e);

                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);

                    // Optionally show message on failure
                    notFoundTextView.setVisibility(View.VISIBLE); // Show "Not Found" message
                    recyclerView.setVisibility(View.GONE); // Hide RecyclerView
                });
    }


}