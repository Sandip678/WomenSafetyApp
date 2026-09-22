package com.womensefty.womensafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.Random;

public class RegistrationActivity extends AppCompatActivity {

    private EditText username, email, mobile, otp, password, countryCode;
    private Button generateOTP, signUpButton;
    private CheckBox showPassword;
    private DatabaseReference mDatabase;
    private String generatedOTP; // Store the generated OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Find Views
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        countryCode = findViewById(R.id.countryCode);
        mobile = findViewById(R.id.mobile);
        otp = findViewById(R.id.otp);
        password = findViewById(R.id.password);
        generateOTP = findViewById(R.id.generateOTP);
        signUpButton = findViewById(R.id.signUpButton);
        showPassword = findViewById(R.id.showPassword);

        // Show/Hide Password
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                password.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        // Generate OTP Button Click
        generateOTP.setOnClickListener(v -> {
            String countryCodeValue = countryCode.getText().toString().trim(); // Get country code
            String mobileNumber = mobile.getText().toString().trim(); // Get mobile number

            // Add + sign if missing
            if (!countryCodeValue.startsWith("+")) {
                countryCodeValue = "+" + countryCodeValue;
            }

            // Validate mobile number
            if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            } else {
                // Combine country code and mobile number
                String fullMobileNumber = countryCodeValue + mobileNumber;

                // Generate and send OTP
                generatedOTP = generateOTP(); // Generate a random OTP
                otp.setVisibility(View.VISIBLE);
                Toast.makeText(this, "OTP sent to " + fullMobileNumber + ": " + generatedOTP, Toast.LENGTH_SHORT).show();
            }
        });

        // Sign Up Button Click
        signUpButton.setOnClickListener(v -> {
            String name = username.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userMobile = mobile.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String userOTP = otp.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userMobile) || TextUtils.isEmpty(userPassword) || TextUtils.isEmpty(userOTP)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (userMobile.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            } else if (!userOTP.equals(generatedOTP)) {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            } else {
                // Save user data to Firebase
                saveUserToFirebase(name, userEmail, userMobile, userPassword);
            }
        });
    }

    // Method to generate a random 6-digit OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Method to save user data to Firebase Realtime Database
    private void saveUserToFirebase(String name, String email, String mobile, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Firebase Authentication में User Register करें
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // यूजर रजिस्ट्रेशन सफल हुआ, अब उसे Realtime Database में सेव करें
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                        // User डेटा स्टोर करें
                        AppUser appUser = new AppUser(name, email, mobile, password);
                        userRef.setValue(appUser).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // अगर Authentication फेल हुआ तो एरर दिखाएँ
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}