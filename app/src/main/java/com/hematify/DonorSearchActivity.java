package com.hematify;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log; // Import Log class
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class DonorSearchActivity extends AppCompatActivity {

    private static final String TAG = "DonorSearchActivity"; // Define a tag for logging
    private Button searchButton;
    private RecyclerView donorsRecyclerView;

    String selectedProvince="";
    private TextInputLayout province,city,bloodType;
    String selectedCity="";
    private DonorAdapter donorAdapter;
    AutoCompleteTextView provinceAutoCompleteTextView ,bloodTypeAutoCompleteTextView,cityAutoCompleteTextView ;
    TextInputLayout provinceLayout,cityLayout,bloodLayout;
    private List<User> donorList = new ArrayList<>();
    private Map<String, List<String>> provinceCityMap = new HashMap<>();
    private List<String> provincesList = new ArrayList<>();
    private List<String> citiesList = new ArrayList<>();
    private List<String> bloodTypesList = new ArrayList<>(Arrays.asList("A +ve", "A -ve", "B +ve", "B -ve", "AB +ve", "AB -ve", "O +ve", "O -ve"));


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donor_search);

        Log.d(TAG, "onCreate started");


        provinceLayout=findViewById(R.id.province_input_layout);
        cityLayout=findViewById(R.id.city_input_layout);
        bloodLayout=findViewById(R.id.blood_type_input_layout);
        searchButton = findViewById(R.id.search_button);
        donorsRecyclerView = findViewById(R.id.donors_recycler_view);
        provinceAutoCompleteTextView = findViewById(R.id.province_auto_complete_text_view);
        bloodTypeAutoCompleteTextView = findViewById(R.id.blood_type_auto_complete_text_view);
        cityAutoCompleteTextView = findViewById(R.id.city_auto_complete_text_view);





        ImageView filterButton = findViewById(R.id.filter_btn);

        // Handle Filter button click


        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            startActivity(new Intent(DonorSearchActivity.this, Bottom_Navigation.class));
            finish();
        });


        filterButton.setOnClickListener(v -> {
            Log.d(TAG, "Filter button clicked");
            if (provinceLayout.getVisibility() == View.GONE) {
                provinceLayout.setVisibility(View.VISIBLE);
                cityLayout.setVisibility(View.VISIBLE);
                bloodLayout.setVisibility(View.VISIBLE);
                donorsRecyclerView.setVisibility(View.GONE);

                Log.d(TAG, "Spinners made visible");
            } else {
                provinceLayout.setVisibility(View.GONE);
                cityLayout.setVisibility(View.GONE);
                bloodLayout.setVisibility(View.GONE);
                donorsRecyclerView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Spinners made gone");
            }
        });


// Load provinces and cities from CSV
        loadProvinceCityData();

        // Set up AutoCompleteTextView for Province
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provincesList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceAutoCompleteTextView.setAdapter(provinceAdapter);

        provinceAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProvince = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected Province: " + selectedProvince);
            updateCitySuggestions(selectedProvince); // Update city suggestions based on selected province
        });

        // Set up AutoCompleteTextView for City
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cityAutoCompleteTextView.setAdapter(cityAdapter);

        cityAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected City: " + selectedCity);
        });

        // Set up AutoCompleteTextView for Blood Type
        ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodTypesList);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeAutoCompleteTextView.setAdapter(bloodTypeAdapter);

        bloodTypeAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBloodType = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected Blood Type: " + selectedBloodType);
        });


        // Set up RecyclerView
        donorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorAdapter = new DonorAdapter(this, donorList, donor -> {
            Toast.makeText(DonorSearchActivity.this, "Request sent to " + donor.getFirst_name(), Toast.LENGTH_SHORT).show();
        });
        donorsRecyclerView.setAdapter(donorAdapter);

        // Search donors when button is clicked
        searchButton.setOnClickListener(v -> {
            donorList.clear();

            // Notify the adapter to refresh the UI and show no results initially
            donorAdapter.notifyDataSetChanged();

            provinceLayout.setVisibility(View.GONE);
            cityLayout.setVisibility(View.GONE);
            bloodLayout.setVisibility(View.GONE);
            donorsRecyclerView.setVisibility(View.VISIBLE);
            searchDonors(); // Call search function
        });
    }

    // Load Province and City Data
    private void loadProvinceCityData() {
        Log.d(TAG, "Loading province and city data");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("pakistan_cities.csv")));
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String city = parts[0].trim();
                    String province = parts[1].trim();

                    Log.d(TAG, "City: " + city + ", Province: " + province);

                    if (!provinceCityMap.containsKey(province)) {
                        provinceCityMap.put(province, new ArrayList<>());
                        provincesList.add(province);
                        Log.d(TAG, "Added province: " + province);
                    }

                    provinceCityMap.get(province).add(city);
                }
            }
            reader.close();

            // Set up adapter for Province AutoCompleteTextView
            ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provincesList);
            provinceAutoCompleteTextView.setAdapter(provinceAdapter);
            Log.d(TAG, "Provinces loaded: " + provincesList);

        } catch (Exception e) {
            Log.e(TAG, "Error loading province and city data", e);
        }
    }


    private void updateCitySuggestions(String province) {
        Log.d(TAG, "Updating city suggestions for province: " + province);
        if (provinceCityMap.containsKey(province)) {
            citiesList = provinceCityMap.get(province);

            // Set up the adapter for city AutoCompleteTextView
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citiesList);
            cityAutoCompleteTextView.setAdapter(cityAdapter);
            Log.d(TAG, "Cities loaded for " + province + ": " + citiesList);
        } else {
            Log.d(TAG, "No cities found for province: " + province);
        }
    }


    private void searchDonors() {
        String selectedProvince = provinceAutoCompleteTextView.getText().toString();
        String selectedCity = cityAutoCompleteTextView.getText().toString();
        String selectedBloodType = bloodTypeAutoCompleteTextView.getText().toString();

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView notFoundTextView = findViewById(R.id.notfound);

        // Show progress bar and hide "not found" text
        progressBar.setVisibility(View.VISIBLE);
        notFoundTextView.setVisibility(View.GONE);

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = fStore.collection("Users");

        usersRef.whereEqualTo("location", selectedCity + ", " + selectedProvince)
                .whereEqualTo("blood_group", selectedBloodType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    donorList.clear();
                    Log.d(TAG, "Number of documents returned: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        // No donors found, show "not found" text
                        notFoundTextView.setVisibility(View.VISIBLE);
                    } else {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User donor = doc.toObject(User.class);
                            if (donor != null) {
                                donor.setId(doc.getId());
                                donorList.add(donor);
                                Log.d(TAG, "Found donor details: ID: " + donor.getId() + ", Name: " + donor.getFirst_name());
                            } else {
                                Log.e(TAG, "Donor object is null for document: " + doc.getId());
                            }
                        }
                        donorAdapter.notifyDataSetChanged();
                        notFoundTextView.setVisibility(View.GONE); // Hide "not found" text if donors are found
                    }

                    // Hide progress bar after loading data
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Total donors after query: " + donorList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching donors", e);
                    Toast.makeText(DonorSearchActivity.this, "Error fetching donors", Toast.LENGTH_SHORT).show();

                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                });
    }

    public void onRequestClick(User donor) {
        String donorID = donor.getId();  // Get the donor's ID
        String donorName = donor.getFirst_name() + " " + donor.getLast_name();
        String bloodGroup = donor.getBlood_group();

        // Create an Intent to navigate to the RequestFormActivity
        Intent intent = new Intent(DonorSearchActivity.this, Request.class);

        // Pass the donor details to the RequestFormActivity via Intent
        intent.putExtra("DONOR_ID", donorID);
        intent.putExtra("DONOR_NAME", donorName);
        intent.putExtra("BLOOD_GROUP", bloodGroup);

        startActivity(intent);
    }


}
