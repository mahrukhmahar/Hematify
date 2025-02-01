package com.hematify.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hematify.DonorRequestActivity;
import com.hematify.DonorSearchActivity;
import com.hematify.R;


import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewFlipper viewFlipper;
    private LinearLayout dotsContainer;
    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private ImageView[] dots;
    private final Handler handler = new Handler();
    private RecyclerView recyclerView;
    private BloodRequestAdapter adapter;
    private List<BloodRequest> bloodRequestList;
    TextView seeMoreButton, notFoundTextView;
    LinearLayout find_donor, post_request, donateNow;
    Button logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        viewFlipper = view.findViewById(R.id.viewFlipper);
        dotsContainer = view.findViewById(R.id.dotsContainer);
        find_donor=view.findViewById(R.id.find_donor);
        donateNow=view.findViewById(R.id.donate);
        notFoundTextView=view.findViewById(R.id.notfound);

        find_donor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "button clicked directing to donor seacrh", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), DonorSearchActivity.class));
            }
        });

        donateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "button clicked directing to donor seacrh", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), Requestors.class));
            }
        });




        setupViewFlipperAndDots();

        // Start automatic flipping and update dots
        startAutoFlipping();

        bloodRequestList = new ArrayList<>(); // Initialize the list that will hold the blood requests

        recyclerView = view.findViewById(R.id.bloodRequestRecyclerView); // Find the RecyclerView by ID
        adapter = new BloodRequestAdapter(getContext(), bloodRequestList); // Create the adapter and pass the list of blood requests
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Set up the layout manager for the RecyclerView
        recyclerView.setAdapter(adapter);

        fetchBloodRequests();

        seeMoreButton=view.findViewById(R.id.seeMoreButton);

        seeMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Requestors.class));
            }
        });


        post_request=view.findViewById(R.id.post_request);
        post_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DonorRequestActivity.class));
            }
        });


        return view;

    }

    private void fetchBloodRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference bloodRequestsRef = db.collection("BloodRequests");

        Log.d("BloodRequest", "Fetching blood requests...");

        // Query to get the blood requests sorted by 'timestamp' in descending order (most recent first)
        bloodRequestsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("BloodRequest", "Query successful, documents found: " + queryDocumentSnapshots.size());

                    bloodRequestList.clear(); // Clear previous data

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("BloodRequest", "No documents found in BloodRequests collection.");
                    }

                    // Loop through the query results
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Log.d("BloodRequest", "Document ID: " + documentSnapshot.getId());

                        // Log the fields in the document
                        Log.d("BloodRequest", "Document data: " + documentSnapshot.getData());

                        // Map the document to a BloodRequest object
                        BloodRequest bloodRequest = documentSnapshot.toObject(BloodRequest.class);

                        if (bloodRequest != null) {
                            // Retrieve the timestamp field directly from the document snapshot
                            Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                            // If the timestamp is not null, log it or use it as needed
                            if (timestamp != null) {
                                Log.d("BloodRequest", "Request timestamp: " + timestamp.toDate());
                                bloodRequest.setTimestamp(timestamp); // Set the timestamp in BloodRequest
                            }

                            // Add the BloodRequest to the list
                            bloodRequestList.add(bloodRequest);
                        } else {
                            Log.d("BloodRequest", "BloodRequest object is null");
                        }
                    }

                    // If there are no requests in the list, show the "Not Found" text
                    if (bloodRequestList.isEmpty()) {
                        Log.d("BloodRequest", "No blood requests found in the list");
                        notFoundTextView.setVisibility(View.VISIBLE);  // Show "Not Found" message
                        recyclerView.setVisibility(View.GONE);          // Hide RecyclerView
                    } else {
                        Log.d("BloodRequest", "Found blood requests: " + bloodRequestList.size());
                        notFoundTextView.setVisibility(View.GONE);     // Hide "Not Found" message
                        recyclerView.setVisibility(View.VISIBLE);      // Show RecyclerView
                    }

                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("BloodRequest", "Error fetching blood requests", e);
                    Toast.makeText(getContext(), "Error fetching blood requests", Toast.LENGTH_SHORT).show();

                    // Handle failure case: Show "Not Found" message
                    notFoundTextView.setVisibility(View.VISIBLE);  // Show "Not Found" message
                    recyclerView.setVisibility(View.GONE);          // Hide RecyclerView
                });
    }





    private void setupViewFlipperAndDots() {
        // Add images to ViewFlipper
        for (int image : images) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(image);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewFlipper.addView(imageView);
        }

        // Create Dots
        dots = new ImageView[images.length];
        for (int i = 0; i < images.length; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageResource(R.drawable.dot_default);
            dots[i].setPadding(5, 0, 5, 0);
            dotsContainer.addView(dots[i]);
        }

        updateDots(0); // Set initial dot
    }

    private void startAutoFlipping() {
        viewFlipper.setFlipInterval(3000); // Flip every 3 seconds
        viewFlipper.startFlipping();

        // Sync dots before the next flip happens
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int nextChild = (viewFlipper.getDisplayedChild() + 1) % images.length;
                updateDots(nextChild); // Update dots just before flipping
                handler.postDelayed(this, 3000); // Repeat every 3 seconds
            }
        }, 3500); // Slightly before ViewFlipper flips
    }


    private void updateDots(int currentPosition) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == currentPosition ? R.drawable.dot_active : R.drawable.dot_default);
        }
    }

}