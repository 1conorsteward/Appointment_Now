/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    RegisterActivity.java

    This file manages the user interface for the account registration process in the AppointmentNow application.
    Users are able to enter their email, password, and re-enter their password for confirmation. They can also opt-in
    to SMS notifications through a checkbox. Upon successful registration, the user is redirected to the event display
    screen, and the new user information is stored in the database.

    Key Features:
    - Allows users to input their email and password to create a new account.
    - Verifies that both password fields match before allowing registration.
    - Provides an option for users to enable or disable SMS notifications.
    - Stores the new user data in an SQLite database through the DatabaseHelper class.
    - Handles the back button to exit the registration process.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    // UI elements for user input fields and SMS notifications checkbox
    private EditText emailField;               // Input field for the user's email
    private EditText passwordField;            // Input field for the user's password
    private EditText reEnterPasswordField;     // Input field for re-entering the user's password for confirmation
    private CheckBox smsNotificationsCheckbox; // Checkbox to enable or disable SMS notifications for the user

    // Database helper for handling SQLite operations
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_account);  // Set the layout for the registration screen

        // Initialize UI components by finding them from the layout resource
        emailField = findViewById(R.id.email);  // Input field for email
        passwordField = findViewById(R.id.password);  // Input field for password
        reEnterPasswordField = findViewById(R.id.re_enter_password);  // Re-enter password field for confirmation
        smsNotificationsCheckbox = findViewById(R.id.sms_notifications);  // Checkbox for SMS notification opt-in
        Button registerButton = findViewById(R.id.register_button);  // Button to submit the registration

        // Initialize the DatabaseHelper for performing database operations
        dbHelper = new DatabaseHelper(this);

        // Handle the physical back button press to exit the registration process
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Close the RegisterActivity when the back button is pressed
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);  // Register the back button callback

        // Set click listener for the custom back button in the UI
        findViewById(R.id.back_button).setOnClickListener(v -> {
            finish();  // Close the RegisterActivity when the back button is clicked
        });

        // Set click listener for the register button
        registerButton.setOnClickListener(v -> {
            // Get user input values from the fields
            String email = emailField.getText().toString();  // Email input from the user
            String password = passwordField.getText().toString();  // Password input from the user
            String reEnterPassword = reEnterPasswordField.getText().toString();  // Confirmation password
            boolean smsPermission = smsNotificationsCheckbox.isChecked();  // SMS notification checkbox status

            // Input validation: check if fields are empty
            if (email.isEmpty() || password.isEmpty() || reEnterPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();  // Display error if fields are empty
            }
            // Check if the password fields match
            else if (!password.equals(reEnterPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();  // Display error if passwords do not match
            }
            // Proceed with registration if input is valid
            else {
                // Save the new user in the database
                long userId = dbHelper.addUser(email, password, smsPermission ? 1 : 0);  // Add user with SMS permission status

                // Check if the user was successfully added
                if (userId != -1) {
                    // Registration successful: navigate to the EventDisplayActivity
                    Intent intent = new Intent(RegisterActivity.this, EventDisplayActivity.class);
                    startActivity(intent);
                    finish();  // Close the RegisterActivity after successful registration
                } else {
                    // Display error message if registration failed
                    Toast.makeText(RegisterActivity.this, "Registration failed, try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

