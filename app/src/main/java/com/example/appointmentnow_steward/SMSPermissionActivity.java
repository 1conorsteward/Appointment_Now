/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    SMSPermissionActivity.java

    This activity is responsible for managing the SMS permission within the AppointmentNow application. It provides
    a user interface that allows users to check, request, and manage the status of the SMS permission for sending messages.
    The activity includes a toggle switch to enable or disable the permission and displays the current status of the SMS
    permission.

    Key Features:
    - Checks if the SEND_SMS permission is granted.
    - Requests SMS permission if it is not already granted.
    - Notifies users that SMS permission cannot be revoked directly from the app and needs to be done via settings.
    - Updates the user interface (UI) based on the permission state.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SwitchCompat;

// This activity handles SMS permission management, including requesting and revoking permissions.
public class SMSPermissionActivity extends AppCompatActivity {

    // Request code for SMS permission
    private static final int SMS_PERMISSION_CODE = 1001;

    // UI components
    private TextView smsPermissionInfo;  // TextView to display the current permission status
    private SwitchCompat permissionToggle;  // Switch to toggle SMS permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Initialize views from the layout
        smsPermissionInfo = findViewById(R.id.sms_permission_info);  // TextView for displaying permission status
        permissionToggle = findViewById(R.id.permission_toggle);  // Toggle switch for SMS permission
        ImageButton closeButton = findViewById(R.id.close_button);  // Close button to exit the activity

        // Set click listener for the close button to finish the activity
        closeButton.setOnClickListener(v -> finish());  // Finishes the activity when the close button is clicked

        // Set up the toggle switch based on the current SMS permission state
        checkSmsPermission();  // Checks the current SMS permission and updates the UI

        // Set a listener for the toggle switch to handle permission changes
        permissionToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestSMSPermission();  // Requests SMS permission if toggled on
            } else {
                handlePermissionRevocation();  // Handles revocation attempt if toggled off
            }
        });
    }

    // Method to check and request SMS permission
    private void checkAndRequestSMSPermission() {
        // Check if SMS permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the SEND_SMS permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            // If permission is already granted, update the UI
            smsPermissionInfo.setText(R.string.permission_granted);  // Update the TextView to show "Permission Granted"
            permissionToggle.setChecked(true);  // Ensure the toggle switch reflects the granted permission
        }
    }

    // Method to check the current SMS permission state and update the UI accordingly
    private void checkSmsPermission() {
        // Check if SMS permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Update UI to indicate permission is denied
            permissionToggle.setChecked(false);  // Toggle switch is off when permission is denied
            smsPermissionInfo.setText(R.string.permission_denied);  // Update the TextView to show "Permission Denied"
        } else {
            // Update UI to indicate permission is granted
            permissionToggle.setChecked(true);  // Toggle switch is on when permission is granted
            smsPermissionInfo.setText(R.string.permission_granted);  // Update the TextView to show "Permission Granted"
        }
    }

    // Method to handle the case where the user attempts to revoke the permission via the toggle switch
    private void handlePermissionRevocation() {
        // Show a toast message indicating that SMS permission needs to be revoked manually
        Toast.makeText(this, "SMS Permission needs to be revoked from the settings manually.", Toast.LENGTH_LONG).show();
        // Set the toggle switch back to 'checked' state as revocation cannot be handled directly by the app
        permissionToggle.setChecked(true);  // Reset the toggle switch to 'on' to indicate permission is still active
    }

    // Callback method to handle the result of a permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the result is for the SMS permission request
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, update the UI
                smsPermissionInfo.setText(R.string.permission_granted);  // Update the TextView to show "Permission Granted"
                permissionToggle.setChecked(true);  // Ensure the toggle switch reflects the granted permission
            } else {
                // If permission is denied, update the UI
                smsPermissionInfo.setText(R.string.permission_denied);  // Update the TextView to show "Permission Denied"
                permissionToggle.setChecked(false);  // Ensure the toggle switch reflects the denied permission
            }
        }
    }
}
