/*
 *     Appointment Now - Register Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity allows users to create an account in the AppointmentNow application.
 *     Users enter an email and password, confirm their password, and opt-in for SMS notifications.
 *     Upon successful registration, users are redirected to the main event display screen.
 *
 *     Features:
 *     - Secure password hashing using SHA-256.
 *     - Prevents duplicate email registration.
 *     - Stores user credentials safely in SQLite.
 *     - Redirects users to the main screen upon successful sign-up.
 *
 *     Dependencies:
 *     - `DatabaseHelper.java` (Handles database operations)
 *     - `EventDisplayActivity.java` (Main app screen)
 *
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private EditText emailField, passwordField, confirmPasswordField;
    private CheckBox smsNotificationsCheckbox;
    private DatabaseHelper dbHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_account);

        initializeUI();
        dbHelper = new DatabaseHelper(this);
    }

    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initializeUI() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.re_enter_password);
        smsNotificationsCheckbox = findViewById(R.id.sms_notifications);
        Button registerButton = findViewById(R.id.register_button);

        // Mask password input
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Register button listener
        registerButton.setOnClickListener(v -> attemptRegistration());

        // Custom back button listener
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    /**
     * Handles the user registration process.
     */
    private void attemptRegistration() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        boolean smsPermission = smsNotificationsCheckbox.isChecked();

        // Validate user input
        if (!validateInput(email, password, confirmPassword)) return;

        // Check if email is already registered
        if (isEmailAlreadyRegistered(email)) {
            showToast("Email already registered. Try logging in.");
            return;
        }

        // Securely hash the password before saving it
        String hashedPassword = dbHelper.hashPassword(password);
        if (hashedPassword == null) {
            showToast("Error processing password. Please try again.");
            return;
        }

        // Register the new user
        long userId = dbHelper.addUser(email, hashedPassword);
        if (userId != -1) {
            navigateToMainScreen();
        } else {
            showToast("Registration failed. Please try again.");
        }
    }

    /**
     * Validates user input fields.
     *
     * @param email           User's email input.
     * @param password        User's password input.
     * @param confirmPassword Password confirmation input.
     * @return True if input is valid, otherwise false.
     */
    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields.");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format.");
            return false;
        }

        if (password.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match.");
            return false;
        }

        return true;
    }

    /**
     * Checks if an email is already registered in the database.
     *
     * @param email The email to check.
     * @return True if email is already registered, otherwise false.
     */
    private boolean isEmailAlreadyRegistered(String email) {
        return dbHelper.isEmailTaken(email);
    }

    /**
     * Navigates to the main event display screen after successful registration.
     */
    private void navigateToMainScreen() {
        Intent intent = new Intent(RegisterActivity.this, EventDisplayActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Displays a toast message.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}