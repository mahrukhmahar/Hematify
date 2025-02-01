package com.hematify;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLocationActivity extends AppCompatActivity {

    private Spinner spinnerProvince, spinnerCity;
    private ArrayAdapter<String> provinceAdapter, cityAdapter;
    private Map<String, List<String>> provinceCityMap = new HashMap<>();
    private List<String> provincesList = new ArrayList<>();
    private List<String> citiesList = new ArrayList<>();
    private String selectedProvince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);

        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCity = findViewById(R.id.spinnerCity);

        // Load the province and city data from the CSV file
        loadProvinceCityData();

        // Check if the provinces list has been populated
        Log.d("UserLocationActivity", "Provinces List: " + provincesList);

        // Set up the Province Spinner
        if (!provincesList.isEmpty()) {
            provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provincesList);
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvince.setAdapter(provinceAdapter);
        } else {
            Log.e("UserLocationActivity", "Provinces list is empty!");
        }

        // Set listener for Province Spinner
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // Get the selected province
                selectedProvince = provincesList.get(position);
                Log.d("UserLocationActivity", "Selected Province: " + selectedProvince);
                updateCitySpinner(selectedProvince);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Ensure the City Spinner works as expected (disable it initially until province is selected)
        spinnerCity.setEnabled(false);
    }

    private void loadProvinceCityData() {
        try {
            // Read the CSV file from the assets folder
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("pakistan_cities.csv")));
            String line;
            reader.readLine(); // Skip the header row (City,Province)
            while ((line = reader.readLine()) != null) {
                // Split the line by comma (assuming the format "City,Province")
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String city = parts[0].trim();
                    String province = parts[1].trim();

                    // Add province if it's not already in the map
                    if (!provinceCityMap.containsKey(province)) {
                        provinceCityMap.put(province, new ArrayList<>());
                        provincesList.add(province);
                    }

                    // Add the city to the list of cities for this province
                    provinceCityMap.get(province).add(city);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCitySpinner(String province) {
        // Check if province has cities associated with it
        if (provinceCityMap.containsKey(province)) {
            citiesList = provinceCityMap.get(province);
            Log.d("UserLocationActivity", "Cities for " + province + ": " + citiesList);

            cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(cityAdapter);

            // Enable the city spinner once the province is selected
            spinnerCity.setEnabled(true);
        } else {
            Log.e("UserLocationActivity", "No cities found for " + province);
        }
    }
}
