/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    EventDetailActivity.java

    This class represents the Activity responsible for displaying the detailed view of a selected
    event (appointment) in the AppointmentNow application. Users can view event details such as
    patient name, doctor name, appointment date, status, notes, and location. Additionally, users
    can export these details as a PDF for offline usage or record keeping.

    Key Features:
    - Displays the full details of an appointment event.
    - Provides functionality to export the event details as a PDF.
    - Handles permissions for writing to external storage when exporting PDFs.
    - Closes the activity if an invalid event ID is passed or event data is missing.

    Permissions:
    - WRITE_EXTERNAL_STORAGE: Required to save the exported PDF to external storage.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import android.Manifest;

public class EventDetailActivity extends AppCompatActivity {

    // Declare UI components for displaying event details
    private TextView patientNameTextView;
    private TextView doctorNameTextView;
    private TextView appointmentDateTextView;
    private TextView statusTextView;
    private TextView notesTextView;
    private TextView locationTextView;

    // Database helper for retrieving event data from the database
    private DatabaseHelper databaseHelper;

    // onCreate() method called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);  // Set the layout for this activity

        // Initialize the views to display event details
        patientNameTextView = findViewById(R.id.patient_name);
        doctorNameTextView = findViewById(R.id.doctor_name);
        appointmentDateTextView = findViewById(R.id.appointment_date);
        statusTextView = findViewById(R.id.status);
        notesTextView = findViewById(R.id.notes);
        locationTextView = findViewById(R.id.location);

        // Initialize close button and set a click listener to close the activity
        ImageButton closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());  // Finish the activity when close button is clicked

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);

        // Get event ID from the intent that started this activity
        Intent intent = getIntent();
        long eventId = intent.getLongExtra("event_id", -1);  // Default to -1 if no event ID is passed

        // Check if the event ID is valid and load event details
        if (eventId != -1) {
            loadEventDetails(eventId);  // Load the event details from the database
        } else {
            // Show an error message if the event ID is invalid and close the activity
            Toast.makeText(this, "Error Finding Event", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Request WRITE_EXTERNAL_STORAGE permission if it's not already granted
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1001);  // Request the required permission
        }

        // Initialize export button and set a click listener to export event details as PDF
        Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> exportVisitAsPDF());  // Export visit as PDF when clicked
    }

    // Method to export the visit details as a PDF
    private void exportVisitAsPDF() {
        // Create a new PDF document
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();  // A4 size page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);  // Start a new page

        // Set up the canvas and paint to write text onto the PDF
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(16);  // Set text size for the PDF content

        // Retrieve text content from the TextViews
        String title = "Visit Details";
        String patientName = "Patient Name: " + patientNameTextView.getText().toString();
        String doctorName = "Doctor Name: " + doctorNameTextView.getText().toString();
        String appointmentDate = "Appointment Date: " + appointmentDateTextView.getText().toString();
        String status = "Status: " + statusTextView.getText().toString();
        String notes = "Notes: " + notesTextView.getText().toString();
        String location = "Location: " + locationTextView.getText().toString();

        // Draw the text onto the PDF using the canvas
        canvas.drawText(title, 50, 50, paint);
        canvas.drawText(patientName, 50, 100, paint);
        canvas.drawText(doctorName, 50, 150, paint);
        canvas.drawText(appointmentDate, 50, 200, paint);
        canvas.drawText(status, 50, 250, paint);
        canvas.drawText(notes, 50, 300, paint);
        canvas.drawText(location, 50, 350, paint);

        // Finish the page
        pdfDocument.finishPage(page);

        // Define the file path to save the PDF
        File pdfFile;
        pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "visit_details.pdf");

        // Write the PDF content to the file
        try {
            pdfDocument.writeTo(Files.newOutputStream(pdfFile.toPath()));
            Toast.makeText(this, "PDF exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // Handle any errors that occur during PDF export
            Log.e("EventDetailActivity", "Error exporting PDF", e);
            Toast.makeText(this, "Error exporting PDF", Toast.LENGTH_SHORT).show();
        }

        // Close the PDF document
        pdfDocument.close();
    }

    // Method to load event details from the database using the event ID
    private void loadEventDetails(long eventId) {
        Cursor cursor = databaseHelper.getEventById(eventId);  // Query the database for the event
        if (cursor != null && cursor.moveToFirst()) {
            // Use actual column names directly instead of getting them from string resources
            patientNameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
            doctorNameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")));
            appointmentDateTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")));
            statusTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            notesTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            locationTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            cursor.close();  // Close the cursor after retrieving the data
        } else {
            // Show an error message if the event is not found and close the activity
            Toast.makeText(this, R.string.event_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
