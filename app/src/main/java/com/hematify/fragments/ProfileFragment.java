package com.hematify.fragments;



import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.google.firebase.firestore.Source;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hematify.Login;
import com.hematify.R;
import com.hematify.User;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class ProfileFragment extends Fragment {

    TextView username, location, blood_group, donated, requested;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    User currentUser; // Declare a User object to hold the fetched user data

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        username = view.findViewById(R.id.userName);
        location = view.findViewById(R.id.location);
        blood_group = view.findViewById(R.id.blood_type);
        donated = view.findViewById(R.id.donated_count);
        requested = view.findViewById(R.id.requested_count);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (currentUser == null) {
            Log.d("ProfileFragment", "User not logged in, redirecting...");
            Intent intent = new Intent(getActivity(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();  // Close current activity
            return null;  // Prevent fragment from rendering
        }

        userId = currentUser.getUid();  // Ensure userId is initialized properly

        fetchUserProfileData(view);


        Button logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void fetchUserProfileData(View view) {
        DocumentReference documentReference = fStore.collection("Users").document(userId);

        // Fetch cached data first for faster UI update
        documentReference.get(Source.CACHE)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUserProfile(documentSnapshot, view);
                    }
                })
                .addOnFailureListener(e -> Log.d("ProfileFragment", "No cached data available"));

        // Fetch latest data from Firestore
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return; // Prevent crash if Fragment is detached
                    if (documentSnapshot.exists()) {
                        updateUserProfile(documentSnapshot, view);
                    } else {
                        Log.d("ProfileFragment", "No document found or snapshot empty");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to fetch profile", e);
                });
    }

    private void updateUserProfile(DocumentSnapshot documentSnapshot, View view) {
        String firstName = capitalizeFirstLetter(documentSnapshot.getString("first_name"));
        String lastName = capitalizeFirstLetter(documentSnapshot.getString("last_name"));
        String bloodGroup = documentSnapshot.getString("blood_group");
        String userLocation = documentSnapshot.getString("location");
        String email = documentSnapshot.getString("email");
        String gender = documentSnapshot.getString("gender");
        String phoneNo = documentSnapshot.getString("phone_no");
        String dateOfBirth = documentSnapshot.getString("date_of_birth");
        String base64Image = documentSnapshot.getString("profileImageBase64");
        Boolean availability = documentSnapshot.getBoolean("availability");

        if (availability == null) availability = true;

        // If no profile image exists, generate one and store it
        if (base64Image == null || base64Image.isEmpty()) {
            Bitmap initialsBitmap = generateInitialsBitmap(firstName, lastName);
            base64Image = encodeBitmapToBase64(initialsBitmap);

            // Save the generated image to Firestore
            documentSnapshot.getReference().update("profileImageBase64", base64Image)
                    .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "Profile image saved"))
                    .addOnFailureListener(e -> Log.e("ProfileFragment", "Error saving profile image", e));
        }

        currentUser = new User(userId, firstName, lastName, bloodGroup, userLocation, email, gender, phoneNo, dateOfBirth, base64Image);

        // Update UI
        updateUI(firstName, lastName, bloodGroup, userLocation, base64Image, view, availability);
    }

    private void updateUI(String firstName, String lastName, String bloodGroup, String userLocation, String base64Image, View view, Boolean availability) {
        username.setText(firstName + " " + lastName);
        blood_group.setText(bloodGroup);
        location.setText(userLocation);

        ImageView profilePic = view.findViewById(R.id.profile_pic);

        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            profilePic.setImageBitmap(decodedBitmap);
        } else {
            Bitmap initialsBitmap = generateInitialsBitmap(firstName, lastName);
            profilePic.setImageBitmap(initialsBitmap);
        }

        SwitchCompat toggleSwitch = view.findViewById(R.id.toggle_switch);
        toggleSwitch.setChecked(availability);
        updateSwitchAppearance(toggleSwitch, availability);

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchAppearance(toggleSwitch, isChecked);
            updateAvailabilityInDatabase(isChecked);
        });
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void updateSwitchAppearance(SwitchCompat toggleSwitch, boolean isOn) {
        if (isOn) {
            toggleSwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            toggleSwitch.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
        } else {
            toggleSwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            toggleSwitch.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.black_v2)));
        }
    }




    private String capitalizeFirstLetter(String name) {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        return ""; // Return an empty string if the name is null or empty
    }

    private void updateAvailabilityInDatabase(boolean isAvailable) {
        if (userId == null) {
            Log.e("ProfileFragment", "User ID is null, cannot update availability");
            return; // Exit if userId is null
        }

        DocumentReference userRef = fStore.collection("Users").document(userId);
        userRef.update("availability", isAvailable)
                .addOnSuccessListener(aVoid -> Log.d("ProfileFragment", "Availability updated successfully"))
                .addOnFailureListener(e -> Log.e("ProfileFragment", "Failed to update availability", e));
    }

    private Bitmap generateInitialsBitmap(String firstName, String lastName) {
        String initials = (firstName != null ? firstName.substring(0, 1).toUpperCase() : "") +
                (lastName != null && !lastName.isEmpty() ? lastName.substring(0, 1).toUpperCase() : "");

        int width = 200, height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Generate random background color
        Random random = new Random();
        int red = 150 + random.nextInt(106);  // Ensures value is between 150-255
        int green = 150 + random.nextInt(106);
        int blue = 150 + random.nextInt(106);

        int bgColor = Color.rgb(red, green, blue);

        Paint paint = new Paint();
        paint.setColor(bgColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);

        // Draw initials in the center
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float yOffset = textHeight / 2 - fontMetrics.bottom;

        canvas.drawText(initials, width / 2, height / 2 + yOffset, paint);

        return bitmap;
    }
}
