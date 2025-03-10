/*
 *     Appointment Now - Login Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity handles user login for the AppointmentNow application.
 *     Users enter their email and password to authenticate.
 *     
 *     Features:
 *     - Validates user credentials securely using SHA-256 hashed passwords.
 *     - Uses `DatabaseHelper.java` to interact with the SQLite database.
 *     - Saves session data using SharedPreferences.
 *     - Provides a link to account registration.
 * 
 *     Dependencies:
 *     - `DatabaseHelper.java` (Handles database interactions)
 *     - `EventDisplayActivity.java` (Main app screen)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // Constants
    private static final String PREF_NAME = "AppPreferences";
    private static final String USER_ID_KEY = "user_id";

    // UI Components
    private EditText emailField, passwordField;
    private DatabaseHelper dbHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUI();
        dbHelper = new DatabaseHelper(this);
    }

    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initializeUI() {
        emailField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        Button createAccountButton = findViewById(R.id.create_account_button);

        // Mask password input
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Set up button click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        createAccountButton.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Attempts to log the user in by validating credentials.
     */
    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter both email and password.");
            return;
        }

        long userId = dbHelper.validateUser(email, password);
        if (userId != -1) {
            saveUserSession(userId);
            navigateToMainScreen();
        } else {
            showToast("Invalid email or password.");
        }
    }

    /**
     * Saves the user ID in SharedPreferences to maintain session data.
     *
     * @param userId The authenticated user's ID.
     */
    private void saveUserSession(long userId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putLong(USER_ID_KEY, userId);
        editor.apply();
    }

    /**
     * Navigates to the main event display screen.
     */
    private void navigateToMainScreen() {
        startActivity(new Intent(this, EventDisplayActivity.class));
        finish();
    }

    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    /**
     * Displays a Toast message.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}