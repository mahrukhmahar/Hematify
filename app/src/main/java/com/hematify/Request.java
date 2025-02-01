package com.hematify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request extends AppCompatActivity {

    private ImageView backButton;
    private EditText patientName, phoneNo, date, reason;
    private AppCompatButton requestButton;
    String selectedGender="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request); // Ensure this matches your XML filename

        // Initialize views
        backButton = findViewById(R.id.back);
        patientName = findViewById(R.id.patientName);
        phoneNo = findViewById(R.id.phone_no);
        date = findViewById(R.id.date);
        reason = findViewById(R.id.reason);
        requestButton = findViewById(R.id.requestButton);

        Intent intent = getIntent();
        String donorID = intent.getStringExtra("DONOR_ID");
        String donorName = intent.getStringExtra("DONOR_NAME");
        String bloodGroup = intent.getStringExtra("BLOOD_GROUP");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        if (!username.isEmpty()) {
            patientName.setText(username);
            patientName.setSelection(username.length()); // Place cursor at end
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

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });





    }

    Boolean Valid(){
        Boolean valid=true;

        if(date.getText().toString().trim().isEmpty()){
            Log.d("selectedDate", "Selected Date: " +date.getText().toString().trim());
            valid=false;
        }
        if (isFieldEmpty(patientName)) {
            patientName.setError("First Name is required");
            valid = false;
        } else if (!isValidName(patientName.getText().toString())) {
            patientName.setError("First Name cannot contain numbers, special characters, or spaces");
            valid = false;
        }
        if (isFieldEmpty(phoneNo)) {
            phoneNo.setError("Phone number is required");
            valid = false;
        } else if (!isValidPhoneNumber(phoneNo.getText().toString())) {
            phoneNo.setError("Invalid phone number format");
            valid = false;
        }
        if(selectedGender.isEmpty()){
            valid = false;
        }
        if(isFieldEmpty(reason)) {
            patientName.setError("Reason is required");
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

    public static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^(\\+92|03)\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
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
            }
            }
        }



    public void saveRequestToFirestore(String donorID, String bloodGroup) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String requestorID;

        // Check if the user is logged in
        if (currentUser != null) {
            requestorID = currentUser.getUid(); // Use the logged-in user's ID
        } else {
            // If no user is logged in, generate a random requestor ID
            requestorID = UUID.randomUUID().toString(); // Generates a random unique ID
        }

        // Get the additional data from the EditText fields
        String patientNameText = patientName.getText().toString().trim();
        String phoneNoText = phoneNo.getText().toString().trim();
        String dateText = date.getText().toString().trim();
        String reasonText = reason.getText().toString().trim();

        // Create a map with the request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestor_id", requestorID);
        requestData.put("donor_id", donorID);
        requestData.put("blood_group", bloodGroup);
        requestData.put("status", "pending");  // Request status, can be "pending", "accepted", etc.
        requestData.put("timestamp", FieldValue.serverTimestamp());  // Time when the request was created
        requestData.put("patient_name", patientNameText);
        requestData.put("phone_no", phoneNoText);
        requestData.put("date", dateText);
        requestData.put("reason", reasonText);

        // Save the request in the "BloodRequests" collection
        db.collection("BloodRequests")
                .add(requestData)
                .addOnSuccessListener(documentReference -> {
                    // Request successfully saved, notify user
                    Log.d("Request", "Request saved successfully!");
                    // You can add additional logic here (e.g., sending notifications)
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., display an error message)
                    Log.w("Request", "Error saving request", e);
                });
    }




    }


