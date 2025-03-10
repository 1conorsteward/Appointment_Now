/*
 *     Appointment Now - SMS Permission Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity manages SMS permissions for the AppointmentNow application.
 *     Users can check, request, and view the status of SMS permissions.
 *
 *     Features:
 *     - Checks if SEND_SMS permission is granted.
 *     - Requests SMS permission if not already granted.
 *     - Guides users on how to revoke permissions manually via settings.
 *     - Dynamically updates the UI based on the permission state.
 *
 *     Dependencies:
 *     - `Android Permissions API`
 *
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SMSPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1001;

    // UI Components
    private TextView smsPermissionInfo;
    private SwitchCompat permissionToggle;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        initializeUI();
        updatePermissionUI();
    }

    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initializeUI() {
        smsPermissionInfo = findViewById(R.id.sms_permission_info);
        permissionToggle = findViewById(R.id.permission_toggle);
        ImageButton closeButton = findViewById(R.id.close_button);

        // Close the activity when the close button is clicked
        closeButton.setOnClickListener(v -> finish());

        // Toggle switch listener
        permissionToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestSmsPermission();
            } else {
                showManualRevocationDialog();
            }
        });
    }

    /**
     * Updates the UI based on the current SMS permission state.
     */
    private void updatePermissionUI() {
        boolean isGranted = isSmsPermissionGranted();
        permissionToggle.setChecked(isGranted);
        smsPermissionInfo.setText(isGranted ? R.string.permission_granted : R.string.permission_denied);
    }

    /**
     * Checks if SMS permission is granted.
     */
    private boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests SMS permission if not already granted.
     */
    private void requestSmsPermission() {
        if (isSmsPermissionGranted()) {
            updatePermissionUI(); // Ensure UI is updated correctly
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            showPermissionRationaleDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    /**
     * Shows a rationale dialog before requesting SMS permission.
     */
    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("SMS Permission Required")
                .setMessage("This app requires SMS permission to send appointment notifications. Please grant it to continue.")
                .setPositiveButton("Allow", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    permissionToggle.setChecked(false);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Displays a dialog informing the user that SMS permission must be revoked manually.
     */
    private void showManualRevocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Manual Permission Revocation")
                .setMessage("SMS permission cannot be revoked from within the app. Please go to your device settings to disable it.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    permissionToggle.setChecked(true); // Reset toggle to reflect actual permission state
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Opens the app settings for manual permission revocation.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Handles the result of the SMS permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            boolean isGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            updatePermissionUI();

            if (!isGranted) {
                showPermissionDeniedDialog();
            }
        }
    }

    /**
     * Shows a dialog when the user denies SMS permission.
     */
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("SMS permission is required for sending appointment notifications. You can enable it in your device settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();
    }
}