package com.hematify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Login extends AppCompatActivity {

    EditText emailA, password;
    ProgressBar progressBar;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailA = findViewById(R.id.input_email);
        password =  findViewById(R.id.input_password);
        progressBar =findViewById(R.id.progressBar);
        fAuth= FirebaseAuth.getInstance();


        Button btnLogin =(Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checks()) {
                    progressBar.setVisibility(View.VISIBLE);
                    String email = String.valueOf(emailA.getText());
                    String pass = String.valueOf(password.getText());

                    fAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Handle successful login
                                        UIUtility.showCustomPopup(Login.this, "Login Successful");
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            startActivity(new Intent(Login.this, Bottom_Navigation.class));
                                            finish();  // Optional: Call finish() to close the Login activity if desired
                                        }, 3000);
                                    } else {
                                        // Handle login failure
                                        String errorMessage = "Authentication Failed";

                                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            errorMessage = "Invalid credentials. Please check your email and password.";
                                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            errorMessage = "No user found with this email address.";
                                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            errorMessage = "This email is already in use.";
                                        } else if (task.getException() != null) {
                                            errorMessage = task.getException().getMessage();
                                        }
                                        UIUtility.showCustomPopup(Login.this, errorMessage);
//                                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });



        TextView registerNow = findViewById(R.id.tv_register_now);
        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });
    }

    public Boolean checks(){
        Register rg=new Register();
        Boolean isValid=true;

        if (rg.isFieldEmpty(emailA)) {
            emailA.setError("Email Address is required");
            isValid = false;
        } else if (!rg.isValidEmail(emailA.getText().toString())) {
            emailA.setError("Invalid email address");
            isValid = false;
        }
        if (rg.isFieldEmpty(password)) {
            password.setError("Password is required");
            isValid = false;
        } else if (password.getText().length() < 8) {
            password.setError("Password must be at least 8 characters long");

        }
        return isValid;
    }

}