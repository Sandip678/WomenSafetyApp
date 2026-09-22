package com.womensefty.womensafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailOrUsername, password;
    private CheckBox showPassword;
    private Button loginButton;
    private TextView registerHere;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI Components
        emailOrUsername = findViewById(R.id.username);
        password = findViewById(R.id.password);
        showPassword = findViewById(R.id.showPassword);
        loginButton = findViewById(R.id.loginButton);
        registerHere = findViewById(R.id.registerHere);

        // Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Show/Hide Password Functionality
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        // Login Button Click Listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = emailOrUsername.getText().toString().trim();
                String userPass = password.getText().toString().trim();

                if (userInput.isEmpty() || userPass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter Email/Username and Password", Toast.LENGTH_SHORT).show();
                } else {
                    if (userInput.contains("@")) {
                        loginUserWithEmail(userInput, userPass);
                    } else {
                        loginUserWithUsername(userInput, userPass);
                    }
                }
            }
        });

        // Register Here Click Listener
        registerHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to Login with Email
    private void loginUserWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToDashboard();

                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to Login with Username
    private void loginUserWithUsername(String username, String password) {
        usersRef.orderByChild("name").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null) {
                            loginUserWithEmail(email, password);
                        } else {
                            Toast.makeText(LoginActivity.this, "User data error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Username", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to Navigate to Dashboard
    private void navigateToDashboard() {
        Toast.makeText(LoginActivity.this, "Login Successful - Go to Dashboard", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        finish();
    }
}
