package com.hematify;

import static com.hematify.UIUtility.showCustomPopup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    private Button submitButton;
    private ImageView back_arrow;
    private Button selectedButton = null;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;


    private TextInputLayout provinceTextInputLayout, cityTextInputLayout;
    private AutoCompleteTextView autoCompleteTextViewProvince, autoCompleteTextViewCity;
    private Map<String, List<String>> provinceCityMap = new HashMap<>();
    private List<String> provincesList = new ArrayList<>();
    private List<String> citiesList = new ArrayList<>();
    private String selectedProvince="";
    private String selectedCity="";


    private EditText firstName, lastName, emailAddress, password, rePassword, dateOfBirth, nationalID, phoneNo, fullAddress;
    private TextInputLayout genderSpinnerLayout, citySpinnerLayout;
    private GridLayout bloodTypeGrid;
    private TextView bloodGroupHeading, termsText, alreadyHaveAccount;
    private String selectedDob = "";
    private String selectedGender = "";
    private Calendar calendar;
    FirebaseFirestore fStore;
    String email, pass, first_name, last_name, dob, bloodG, location, phone_no, gender;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        emailAddress = findViewById(R.id.emailAddress);
        password = findViewById(R.id.password);
        rePassword = findViewById(R.id.re_password);
        dateOfBirth =findViewById(R.id.dateofB);
        phoneNo = findViewById(R.id.phone_no);
        genderSpinnerLayout = findViewById(R.id.gender_spinner);
        bloodGroupHeading = findViewById(R.id.blood_group_heading);
        bloodTypeGrid = findViewById(R.id.bloodTypeGroup);
        submitButton = findViewById(R.id.submit_button);
        alreadyHaveAccount = findViewById(R.id.tv_register_now);
        provinceTextInputLayout = findViewById(R.id.province);
        cityTextInputLayout = findViewById(R.id.city);
        autoCompleteTextViewProvince = findViewById(R.id.autoCompleteTextViewProvince);
        autoCompleteTextViewCity = findViewById(R.id.autoCompleteTextViewCity);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar= findViewById(R.id.progressBar);
        fStore= FirebaseFirestore.getInstance();




        calendar = Calendar.getInstance();


        if(firebaseAuth.getCurrentUser() !=null){
            startActivity(new Intent(Register.this, Bottom_Navigation.class));
            finish();
        }


//        DATE OF BIRTH
        dateOfBirth.setOnClickListener(v -> openDatePicker());

//        CITY

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



//        GENDER

        // Get reference to the gender options
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


        submitButton.setOnClickListener(v -> {
            Log.d("RegisterActivity", "Submit button clicked");

            // Show the progress bar while validating and performing operations
            Log.d("RegisterActivity", "Validation started");

            // Call the validation method
            boolean isValid = validateFields();
            if (isValid) {
                Log.d("RegisterActivity", "Validation successful, creating user");
                progressBar.setVisibility(View.VISIBLE);

                email = emailAddress.getText().toString().trim();
                pass = password.getText().toString().trim();
                first_name = firstName.getText().toString().trim();
                last_name = lastName.getText().toString().trim();
                dob = String.valueOf(selectedDob); // Assuming selectedDob is a date field
                bloodG = String.valueOf(selectedButton.getText()); // Assuming bloodGroup is selected from a grid or spinner
                phone_no = phoneNo.getText().toString().trim();
                gender = String.valueOf(selectedGender); // Assuming you have a spinner for gender
                String province = String.valueOf(autoCompleteTextViewProvince.getText());
                String city = String.valueOf(autoCompleteTextViewCity.getText());
                location = city + ", " + province;

                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);  // Hide the progress bar once the task completes
                            if (task.isSuccessful()) {
                                // If user creation is successful, log success and transition
                                Log.d("RegisterActivity", "Account creation successful");

                                UIUtility.showCustomPopup(Register.this, "Account Created. Please verify your email.");

                                // Send the verification email
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(emailVerificationTask -> {
                                                if (emailVerificationTask.isSuccessful()) {
                                                    Log.d("RegisterActivity", "Verification email sent.");
                                                    // Show message to check email for verification
                                                    UIUtility.showCustomPopup(Register.this, "Please check your email for a verification link.");

                                                    // Start checking for email verification status
                                                    checkEmailVerification(user);
                                                } else {
                                                    Log.e("RegisterActivity", "Failed to send verification email.");
                                                }
                                            });
                                }
                            } else {
                                // Handle sign-up failure
                                String errorMessage = "Sign-Up Failed";
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    // Email already in use
                                    errorMessage = "This email is already registered. Please use a different email.";
                                } else if (task.getException() != null) {
                                    // Fallback to general error message
                                    errorMessage = task.getException().getMessage();
                                }
                                UIUtility.showCustomPopup(Register.this, errorMessage);
                            }
                        });
            } else {
                // If validation fails, log that validation failed
                Log.d("RegisterActivity", "Validation failed");
                progressBar.setVisibility(View.GONE);
                UIUtility.showCustomPopup(Register.this, "Please fill in all required fields before submitting.");
            }
        });




        back_arrow = findViewById(R.id.back);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Choose.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });



        }


    private void checkEmailVerification(FirebaseUser user) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            user.reload(); // Reload the user to get the updated verification status
            if (user.isEmailVerified()) {
                Log.d("RegisterActivity", "Email verified, saving user data to Firestore.");
                saveUserDataToFirestore(user); // Save user data to Firestore
                startActivity(new Intent(Register.this, Bottom_Navigation.class));
                finish();
            } else {
                Log.d("RegisterActivity", "Email not verified yet, checking again...");
                checkEmailVerification(user); // Check again after 2 seconds
            }
        }, 1000); // 2 seconds delay before checking again
    }

    private void saveUserDataToFirestore(FirebaseUser user) {
        String userID = user.getUid();
        boolean availability=true;
        DocumentReference documentReference = fStore.collection("Users").document(userID);
        Map<String, Object> usern = new HashMap<>();
        usern.put("first_name", first_name);
        usern.put("last_name", last_name);
        usern.put("email", email);
        usern.put("phone_no", phone_no);
        usern.put("date_of_birth", dob);
        usern.put("gender", gender);
        usern.put("blood_group", bloodG);
        usern.put("location", location);
        usern.put("availability",availability);

        documentReference.set(usern).addOnSuccessListener(unused -> {
            Log.d(TAG, "onSuccess: User Profile created for " + userID);
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Failed to save user data. " + e.getMessage());
        });
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


    private boolean validateFields() {
        boolean isValid = true; // Flag to track validity

        // Check if First Name is empty or contains invalid characters
        if (isFieldEmpty(firstName)) {
            firstName.setError("First Name is required");
            isValid = false;
        } else if (!isValidName(firstName.getText().toString())) {
            firstName.setError("First Name cannot contain numbers, special characters, or spaces");
            isValid = false;
        }

        // Check if Last Name is empty or contains invalid characters
        if (isFieldEmpty(lastName)) {
            lastName.setError("Last Name is required");
            isValid = false;
        } else if (!isValidName(lastName.getText().toString())) {
            lastName.setError("Last Name cannot contain numbers, special characters, or spaces");
            isValid = false;
        }

        // Email validation
        if (isFieldEmpty(emailAddress)) {
            emailAddress.setError("Email Address is required");
            isValid = false;
        } else if (!isValidEmail(emailAddress.getText().toString())) {
            emailAddress.setError("Invalid email address");
            isValid = false;
        }

        // Password validation
        if (isFieldEmpty(password)) {
            password.setError("Password is required");
            isValid = false;
        } else if (!isStrongPassword(password.getText().toString())) {
            password.setError("Password must contain at least one uppercase, one lowercase letter, and one number");
            isValid = false;
        }

        // Confirm password validation
        if (isFieldEmpty(rePassword)) {
            rePassword.setError("Confirm Password is required");
            isValid = false;
        } else if (!password.getText().toString().trim().equals(rePassword.getText().toString().trim())) {
            rePassword.setError("Passwords do not match");
            isValid = false;
        }

        // Date of Birth validation
        if (TextUtils.isEmpty(selectedDob)) {
            Log.d("Error in Dob", "Error here : " + selectedDob);
            dateOfBirth.setError("Date of Birth is required");
            isValid = false;
        }

        // Gender validation
        if (selectedGender.isEmpty()) {
            Log.d("Error in Gender", "Gender is empty");
            isValid = false;
        }

        // Phone number validation
        if (isFieldEmpty(phoneNo)) {
            phoneNo.setError("Phone number is required");
            isValid = false;
        } else if (!isValidPhoneNumber(phoneNo.getText().toString())) {
            phoneNo.setError("Invalid phone number format");
            isValid = false;
        }

        // Province validation
        if (selectedProvince.isEmpty()) {
            Log.d("selectedProvince", "Selected Province: " + selectedProvince);
            isValid = false;
        }

        // City validation
        if (selectedCity.isEmpty()) {
            Log.d("selectedCity", "Selected City: " + selectedCity);
            isValid = false;
        }

        // Button validation (check if selectedButton is set)
        if (selectedButton == null) {
            Log.d("selectedButton", "Selected Button is null");
            isValid = false;
        }

        return isValid;
    }


    //    Name
private boolean isValidName(String name) {
    String nameRegex = "^[A-Za-z]+$";  // Regex to allow only alphabets
    return name.matches(nameRegex);
}


    public boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void openDatePicker() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth1) -> {
            // Format selected date and set it in the EditText
            selectedDob = dayOfMonth1 + "/" + (month1 + 1) + "/" + year1;
            dateOfBirth.setText(selectedDob);

            // Validate date of birth
            if (!validateDob()) {
                // If validation fails, don't allow the user to proceed further or reset the selected date.
                selectedDob = ""; // Clear the selected date if invalid
                dateOfBirth.setText(""); // Optionally clear the EditText as well
            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    // Validate Date of Birth: User must be between 18 and 60 years
    private boolean validateDob() {
        if (TextUtils.isEmpty(selectedDob)) {
            return true; // Allow empty field (optional)
        }

        // Get the current date
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);

        // Extract the year, month, and day from the selected DOB
        String[] dobParts = selectedDob.split("/");
        int dobYear = Integer.parseInt(dobParts[2]);
        int dobMonth = Integer.parseInt(dobParts[1]);
        int dobDay = Integer.parseInt(dobParts[0]);

        // Calculate the age
        int age = currentYear - dobYear;
        if (today.get(Calendar.MONTH) + 1 < dobMonth || (today.get(Calendar.MONTH) + 1 == dobMonth && today.get(Calendar.DAY_OF_MONTH) < dobDay)) {
            age--; // If birthday hasn't happened yet this year, subtract 1 year from age
        }

        // Check if age is between 18 and 60
        if (age < 18 || age > 60) {
            showCustomPopup(this, "You must be between 18 and 60 years old.");
            return false;
        }

        return true;
    }

//CITY
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

//    Password Validation

    public static boolean isStrongPassword(String password) {
        password = password.trim();
        // Password criteria: at least 8 characters, must contain lowercase, uppercase, and digits, optionally special characters
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // Method to validate Pakistan phone number (with +92 or 03)
    public static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^(\\+92|03)\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }


//Email

    public boolean isValidEmail(String email) {
        // Simple email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }



}

