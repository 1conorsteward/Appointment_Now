/*
 *     Appointment Now - Event Detail Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity displays the details of a selected appointment event.
 *     Users can view event details and export them as a PDF.
 *     
 *     Features:
 *     - Displays event details: patient name, doctor name, date, status, notes, and location.
 *     - Exports event details to a PDF file for record-keeping.
 *     - Handles permissions for writing to external storage dynamically.
 * 
 *     Dependencies:
 *     - `DatabaseHelper.java` (Handles database queries)
 *     - `PdfDocument` (Android PDF API for exporting visit details)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EventDetailActivity extends AppCompatActivity {

    // UI Components
    private TextView patientNameTextView, doctorNameTextView, appointmentDateTextView,
            statusTextView, notesTextView, locationTextView;

    // Database helper
    private DatabaseHelper databaseHelper;

    // Constants
    private static final String TAG = "EventDetailActivity";
    private static final String PDF_FILENAME = "visit_details.pdf";

    // Event ID
    private long eventId;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initializeUI();
        databaseHelper = new DatabaseHelper(this);

        eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId != -1) {
            loadEventDetails(eventId);
        } else {
            showErrorAndExit("Error Finding Event");
        }
    }

    /**
     * Initializes UI components and sets up button click listeners.
     */
    private void initializeUI() {
        patientNameTextView = findViewById(R.id.patient_name);
        doctorNameTextView = findViewById(R.id.doctor_name);
        appointmentDateTextView = findViewById(R.id.appointment_date);
        statusTextView = findViewById(R.id.status);
        notesTextView = findViewById(R.id.notes);
        locationTextView = findViewById(R.id.location);

        ImageButton closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> requestStoragePermission());
    }

    /**
     * Handles dynamic permission request for storage.
     */
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    exportVisitAsPDF();
                } else {
                    Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Checks and requests storage permission before exporting PDF.
     */
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            exportVisitAsPDF();
        }
    }

    /**
     * Loads event details from the database using the event ID.
     */
    private void loadEventDetails(long eventId) {
        try (Cursor cursor = databaseHelper.getEventById(eventId)) {
            if (cursor != null && cursor.moveToFirst()) {
                patientNameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
                doctorNameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
                appointmentDateTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")));
                statusTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                notesTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                locationTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            } else {
                showErrorAndExit("Event not found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading event details", e);
            showErrorAndExit("Failed to load event.");
        }
    }

    /**
     * Exports event details as a PDF file.
     */
    private void exportVisitAsPDF() {
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), PDF_FILENAME);

        try (PdfDocument pdfDocument = new PdfDocument();
             FileOutputStream fos = new FileOutputStream(pdfFile)) {

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(16);

            // Draw text content on the PDF
            drawTextOnCanvas(canvas, paint);

            pdfDocument.finishPage(page);
            pdfDocument.writeTo(fos);

            Toast.makeText(this, "PDF exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error exporting PDF", e);
            Toast.makeText(this, "Error exporting PDF", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Draws event details on the PDF canvas.
     */
    private void drawTextOnCanvas(Canvas canvas, Paint paint) {
        int yPosition = 50;
        int lineSpacing = 50;

        String[] eventDetails = {
                "Visit Details",
                "Patient Name: " + patientNameTextView.getText().toString(),
                "Doctor Name: " + doctorNameTextView.getText().toString(),
                "Appointment Date: " + appointmentDateTextView.getText().toString(),
                "Status: " + statusTextView.getText().toString(),
                "Notes: " + notesTextView.getText().toString(),
                "Location: " + locationTextView.getText().toString()
        };

        for (String detail : eventDetails) {
            canvas.drawText(detail, 50, yPosition, paint);
            yPosition += lineSpacing;
        }
    }

    /**
     * Shows an error message and closes the activity.
     */
    private void showErrorAndExit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}