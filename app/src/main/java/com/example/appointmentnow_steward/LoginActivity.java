/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    LoginActivity.java

    This class is responsible for managing the login interface of the AppointmentNow application.
    It allows users to input their credentials (username and password) to log into the app. Upon
    successful login, the user is directed to the main event display screen (EventDisplayActivity).
    If the user does not have an account, they can navigate to the registration screen to create one.

    Key Features:
    - Allows users to input their username and password for authentication.
    - Validates user credentials using a local SQLite database.
    - Saves the user's session by storing their user_id in SharedPreferences upon successful login.
    - Provides a link to the account creation page for new users.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity handles the user interface for logging in and creating a new account.
 * It interacts with the database to authenticate users and manage sessions.
 */
public class LoginActivity extends AppCompatActivity {

    // Input fields for username and password
    private EditText username; // Input field for the username
    private EditText password; // Input field for the password

    // DatabaseHelper instance to perform database operations
    private DatabaseHelper dbHelper; // Database helper for user validation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Set the layout for the login screen

        // Initialize the input fields and buttons by finding them from the layout resource
        username = findViewById(R.id.username);  // Username input field
        password = findViewById(R.id.password);  // Password input field
        Button loginButton = findViewById(R.id.login_button);  // Button to initiate the login process
        Button createAccountButton = findViewById(R.id.create_account_button);  // Button to open the create account screen

        // Initialize the DatabaseHelper instance
        dbHelper = new DatabaseHelper(this);

        // Set the input type for the password field to mask the input with ***
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener(v -> {
            // Retrieve user input from the username and password fields
            String user = username.getText().toString();
            String pass = password.getText().toString();

            // Validate the login credentials
            if (validateLogin(user, pass)) {
                // If login is successful, retrieve the user ID and store it in SharedPreferences
                long userId = getUserId(user);  // Fetch the user ID from the database based on the email/username

                if (userId != -1) {
                    // Save the user_id in SharedPreferences for session management
                    SharedPreferences sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putLong("user_id", userId); // Store the user_id
                    editor.apply();  // Apply the changes to SharedPreferences

                    // Navigate to the EventDisplayActivity upon successful login
                    Intent intent = new Intent(LoginActivity.this, EventDisplayActivity.class);
                    startActivity(intent);
                    finish();  // Close the LoginActivity to prevent back navigation to login
                } else {
                    // Display an error message if the user ID is not found
                    Toast.makeText(LoginActivity.this, "Error retrieving user ID.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Display an error message if the login fails (invalid credentials)
                Toast.makeText(LoginActivity.this, getString(R.string.password_fail), Toast.LENGTH_SHORT).show();
            }
        });

        // Set an OnClickListener for the create account button
        createAccountButton.setOnClickListener(v -> {
            // Start the RegisterActivity to create a new account
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);  // Navigate to the registration screen
        });
    }

    /**
     * Method to validate user login credentials.
     * It checks the email and password against the stored data in the database.
     *
     * @param email    The user's email/username input.
     * @param password The user's password input.
     * @return True if the credentials are valid, false otherwise.
     */
    private boolean validateLogin(String email, String password) {
        // Query the database to check for matching email and password
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + dbHelper.getTableUsers() + " WHERE " +
                        dbHelper.getColumnUserEmail() + "=? AND " +
                        dbHelper.getColumnUserPassword() + "=?",
                new String[]{email, password});  // Query for matching credentials

        if (cursor != null && cursor.moveToFirst()) {
            // If a matching user is found, close the cursor and return true
            cursor.close();
            return true;
        }

        if (cursor != null) {
            cursor.close();  // Close the cursor if no matching user is found
        }
        return false;  // Return false if the login validation fails
    }

    /**
     * Method to retrieve the user ID from the database based on the email.
     * This user ID is stored in SharedPreferences to maintain the session.
     *
     * @param email The user's email input.
     * @return The user's ID if found, -1 otherwise.
     */
    private long getUserId(String email) {
        // Query the database to retrieve the user ID based on the email
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT " + dbHelper.getColumnUserId() + " FROM " + dbHelper.getTableUsers() +
                        " WHERE " + dbHelper.getColumnUserEmail() + " = ?",
                new String[]{email});  // Query for user ID based on email

        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve the user ID from the database cursor
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow(dbHelper.getColumnUserId()));
            cursor.close();  // Close the cursor after retrieving the user ID
            return userId;  // Return the user ID
        }

        if (cursor != null) {
            cursor.close();  // Close the cursor if no user ID is found
        }

        return -1;  // Return -1 if the user ID is not found
    }
}
