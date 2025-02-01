package com.hematify;

import static com.hematify.Register.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DonorRequestActivity extends AppCompatActivity {

    Button selectedButton=null;
    String selectedGender="";
    EditText date;
    AutoCompleteTextView autoCompleteTextViewProvince, autoCompleteTextViewCity;
    private String selectedProvince="";
    private String selectedCity="";
    private String selectedDate = "";
    private TextInputLayout cityTextInputLayout;
    private Map<String, List<String>> provinceCityMap = new HashMap<>();
    private List<String> provincesList = new ArrayList<>();
    private List<String> citiesList = new ArrayList<>();
    EditText name,phoneNo;
    Button sendRequest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_request);

        autoCompleteTextViewProvince=findViewById(R.id.autoCompleteTextViewProvince);
        autoCompleteTextViewCity=findViewById(R.id.autoCompleteTextViewCity);
        date=findViewById(R.id.date);
        sendRequest=findViewById(R.id.requestButton);
        name=findViewById(R.id.patientName);
        phoneNo=findViewById(R.id.phone_no);
        cityTextInputLayout=findViewById(R.id.city);

        // Add TextWatcher to validate DD/MM/YY format
        date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to do anything before text change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateDateInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Button[] buttons = {
                findViewById(R.id.btn_a_positive),
                findViewById(R.id.btn_b_positive),
                findViewById(R.id.btn_o_positive),
                findViewById(R.id.btn_ab_positive),
                findViewById(R.id.btn_a_negative),
                findViewById(R.id.btn_b_negative),
                findViewById(R.id.btn_o_negative),
                findViewById(R.id.btn_ab_negative)
        };

        for (Button button : buttons) {
            button.setOnClickListener(view -> selectBloodGroup(button));
        }


//        Gender

        String[] genderOptions = getResources().getStringArray(R.array.gender_options);

        // Create an ArrayAdapter using the dropdown item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, genderOptions);

        // Reference the AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        // Set adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedGender = (String) parent.getItemAtPosition(position);
            autoCompleteTextView.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
        });



        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            startActivity(new Intent(DonorRequestActivity.this, Bottom_Navigation.class));
            finish();
        });



//        City

        loadProvinceCityData();

        // Set up Province AutoCompleteTextView
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provincesList);
        autoCompleteTextViewProvince.setAdapter(provinceAdapter);


        // Set listener for Province selection
        autoCompleteTextViewProvince.setOnItemClickListener((parent, view, position, id) -> {
            selectedProvince = parent.getItemAtPosition(position).toString();
            cityTextInputLayout.setVisibility(View.VISIBLE);
            updateCitySuggestions(selectedProvince);
        });
        autoCompleteTextViewCity.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = parent.getItemAtPosition(position).toString();
        });

        sendRequest.setOnClickListener(v -> {
            Log.d("BloodRequest", "Send Request button clicked");

            if (Valid()) {  // Ensure all fields are valid
                Log.d("BloodRequest", "Validation successful, posting request");

                // Get user inputs
                String userId = "Guest";  // Default for non-logged-in users
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    userId = currentUser.getUid();
                    // Assign actual UID if logged in
                }

                String phone_no = phoneNo.getText().toString().trim();
                String patientName = name.getText().toString().trim();
                String bloodType = selectedButton.getText().toString().trim();
                String province = autoCompleteTextViewProvince.getText().toString();
                String city = autoCompleteTextViewCity.getText().toString();
                String location = city + ", " + province;


                // Convert selectedDeadline (String) from "DD/MM/YY" to Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                Date deadline = null;
                try {
                    deadline = dateFormat.parse(selectedDate);
                    Log.d("BloodRequest", "Deadline parsed successfully: " + deadline);
                } catch (ParseException e) {
                    Log.e("BloodRequest", "Error parsing deadline: " + e.getMessage());
                    UIUtility.showCustomPopup(DonorRequestActivity.this, "Invalid date format. Please select a valid deadline.");
                    return;
                }

                // Post request to Firestore
                postBloodRequest(userId, phone_no, patientName, bloodType, location, deadline);
                Log.d("BloodRequest", "Request posted successfully for user: " + userId);

                // Delete expired requests
                deleteExpiredRequests();
                Log.d("BloodRequest", "Expired requests deletion triggered");

                // Show success message
                UIUtility.showCustomPopup(DonorRequestActivity.this, "Request sent successfully!");

                // Navigate to BottomNavigation after 2 seconds delay
                Log.d("BloodRequest", "Navigating to Bottom_Navigation");
                startActivity(new Intent(DonorRequestActivity.this, Bottom_Navigation.class));
                finish();
                 // Delay to allow user to see the success message

            } else {
                Log.d("BloodRequest", "Validation failed");
                UIUtility.showCustomPopup(DonorRequestActivity.this, "Please fill in all required fields.");
            }
        });







    }


    Boolean Valid(){
        Boolean valid=true;
        if(selectedButton == null){
            valid=false;
        }
        if(selectedDate.isEmpty()){
            Log.d("selectedDate", "Selected Date: " + selectedDate);
            valid=false;
        }
        if (selectedProvince.isEmpty()) {
            Log.d("selectedProvince", "Selected Province: " + selectedProvince);
            valid = false;
        }

        // City validation
        if (selectedCity.isEmpty()) {
            Log.d("selectedCity", "Selected City: " + selectedCity);
            valid = false;
        }
        if (isFieldEmpty(name)) {
            name.setError("First Name is required");
            valid = false;
        } else if (!isValidName(name.getText().toString())) {
            name.setError("First Name cannot contain numbers, special characters, or spaces");
            valid = false;
        }
        if (isFieldEmpty(phoneNo)) {
            phoneNo.setError("Phone number is required");
            valid = false;
        } else if (!isValidPhoneNumber(phoneNo.getText().toString())) {
            phoneNo.setError("Invalid phone number format");
            valid = false;
        }
        return valid;
    }

    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z]+(\\s[A-Za-z]+)*$";  // Allows multiple words with spaces
        return name.matches(nameRegex);
    }

    public boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }



    private void selectBloodGroup(Button button) {
        if (selectedButton != null) {
            selectedButton.setSelected(false);
            selectedButton.setTextColor(getResources().getColor(R.color.black_v2)); // Reset to default color
        }
        button.setSelected(true);
        selectedButton = button;
        button.setTextColor(getResources().getColor(R.color.red)); // Set to red color
        Log.d("BloodGroupSelection", "Selected Blood Group: " + selectedButton.getText());
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^(\\+92|03)\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }



    public void loadProvinceCityData() {
        try {
            // Read the CSV file from assets
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("pakistan_cities.csv")));
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String city = parts[0].trim();
                    String province = parts[1].trim();

                    // Add province if not already in map
                    if (!provinceCityMap.containsKey(province)) {
                        provinceCityMap.put(province, new ArrayList<>());
                        provincesList.add(province);
                    }

                    // Add city to the province's city list
                    provinceCityMap.get(province).add(city);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCitySuggestions(String province) {
        // Filter the cities based on selected province
        if (provinceCityMap.containsKey(province)) {
            citiesList = provinceCityMap.get(province);
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, citiesList);
            autoCompleteTextViewCity.setAdapter(cityAdapter);
        } else {
            Toast.makeText(this, "No cities found for " + province, Toast.LENGTH_SHORT).show();
        }
    }

    private void validateDateInput() {
        String inputDate = date.getText().toString().trim();

        // Regex for DD/MM/YY format
        String datePattern = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{2}$";

        if (!inputDate.matches(datePattern)) {
            // Show error message if the format is incorrect
            date.setError("Please enter date in DD/MM/YY format");
        } else {
            // Reset error if format is correct
            date.setError(null);

            // Now validate if the date is not less than today's date
            if (!isValidDate(inputDate)) {
                // If the input date is less than today's date
                date.setError("Date cannot be in the past");
            } else {
                // If the input date is valid, store it
                selectedDate = inputDate;
            }
        }
    }

    // Check if the input date is not in the past
    private boolean isValidDate(String inputDate) {
        try {
            // Convert input date (DD/MM/YY) to Date object
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            Date enteredDate = sdf.parse(inputDate);

            // Get today's date
            Date today = new Date();

            // Compare the entered date with today's date
            return enteredDate != null && !enteredDate.before(today);
        } catch (Exception e) {
            // Handle parsing error (e.g., invalid date format)
            e.printStackTrace();
            return false;
        }
    }


    private void postBloodRequest(String userId,String phone_no, String patientName, String blood_type, String location, Date deadline) {
        // Create a new request object
        Map<String, Object> bloodRequest = new HashMap<>();
        bloodRequest.put("userId", userId);
        bloodRequest.put("bloodType", blood_type);
        bloodRequest.put("patientName",patientName);
        bloodRequest.put("phone_no",phone_no);
        bloodRequest.put("location", location);
        bloodRequest.put("timestamp", FieldValue.serverTimestamp());
        bloodRequest.put("deadline", deadline); // Save the deadline as a timestamp
        bloodRequest.put("status", "pending");

        // Save the request to Firestore
        FirebaseFirestore.getInstance().collection("BloodRequests")
                .add(bloodRequest)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BloodRequest", "Request added with ID: " + documentReference.getId());
//                    sendNotificationToAvailableDonors(amount, location); // Notify users
                    updateRequestCount(userId);

                })
                .addOnFailureListener(e -> Log.e("BloodRequest", "Error adding request", e));
    }


    private void deleteExpiredRequests() {
        FirebaseFirestore.getInstance().collection("BloodRequests")
                .whereIn("status", Arrays.asList("pending", "fulfilled"))  // Check for both pending and fulfilled requests
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String status = document.getString("status");

                            // Check if the status is "pending" and deadline is passed
                            if (status != null && status.equals("pending")) {
                                Date deadline = document.getDate("deadline");
                                if (deadline != null && deadline.before(new Date())) {
                                    // Delete expired request
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> Log.d("BloodRequest", "Expired pending request deleted: " + document.getId()))
                                            .addOnFailureListener(e -> Log.e("BloodRequest", "Error deleting expired request", e));
                                }
                            }

                            // Check if the status is "fulfilled"
                            if (status != null && status.equals("fulfilled")) {
                                // Delete fulfilled request (assuming you want to delete all fulfilled requests too)
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> Log.d("BloodRequest", "Fulfilled request deleted: " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("BloodRequest", "Error deleting fulfilled request", e));
                            }
                        }
                    } else {
                        Log.e("BloodRequests", "Error getting documents: ", task.getException());
                    }
                });
    }



    private void updateRequestCount(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(userId);

        // Check if requestCount exists, if not initialize it to 0
        userRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // If requestCount field exists, increment it
                            userRef.update("requestCount", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("User", "Request count updated successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("User", "Error updating request count.", e);
                                    });
                        } else {
                            // If the user doesn't exist, create a new user with requestCount initialized
                            Map<String, Object> user = new HashMap<>();
                            user.put("requestCount", 1); // Initialize with 1 if it's the first request
                            userRef.set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("User", "New user created with requestCount initialized.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("User", "Error initializing user request count.", e);
                                    });
                        }
                    } else {
                        Log.e("User", "Error getting user document", task.getException());
                    }
                });
    }





}