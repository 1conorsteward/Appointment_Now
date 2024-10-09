/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

   EventDisplayActivity.java
    This class is responsible for displaying all the events (appointments) created by the logged-in user.
  It provides functionality to:
    - Add new events via the AddEventDialogFragment.
    - View event details by opening EventDetailActivity.
    - Update or delete existing events.
    - Load events from the SQLite database associated with the logged-in user.
    - Logout the user and clear their session information.
    - Handle SMS permission requests.
   Key Features:
    - Event data is displayed in a GridView format.
    - Integration with SQLite database for storing and retrieving events.
    - User authentication and session management using SharedPreferences.

    Issues: No known issues
 */

package com.example.appointmentnow_steward;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class EventDisplayActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;  // Constant for SMS permission request

    // List to hold the events for the currently logged-in user
    private ArrayList<Event> eventList;

    // Adapter to bind event data to the GridView
    private EventAdapter eventAdapter;

    // Database helper to interact with SQLite database
    private DatabaseHelper databaseHelper;

    /**
     * onCreate() is called when the activity is first created.
     * This method initializes the UI components and loads event data.
     *
     * @param savedInstanceState If the activity is being re-initialized, this is the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_display);  // Set the layout for displaying events

        // Initialize UI components
        GridView eventGrid = findViewById(R.id.event_grid);  // GridView to display the list of events
        Button addEventButton = findViewById(R.id.add_event_button);  // Button to add a new event
        ImageButton smsPermissionButton = findViewById(R.id.sms_permission_button);  // Button for SMS permissions
        ImageButton logoutButton = findViewById(R.id.action_logout);  // Button to log out the user

        // Initialize the DatabaseHelper to interact with the SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Load the events from the database and display them
        loadEvents();

        // Initialize the EventAdapter and set it to the GridView
        eventAdapter = new EventAdapter(this, eventList);
        eventGrid.setAdapter(eventAdapter);

        // Set up item click listener for the GridView to open event details
        eventGrid.setOnItemClickListener((parent, view, position, id) -> {
            Event clickedEvent = eventList.get(position);
            openEventDetailActivity(clickedEvent.getId());  // Open event details for the selected event
        });

        // Set up the Add Event button to open the AddEventDialogFragment
        addEventButton.setOnClickListener(v -> openAddOrEditEventDialog(null, -1));  // Open the dialog with null for a new event

        // Set up the Logout button to log out the user and clear their session
        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();  // Clear all stored user information
            editor.apply();

            eventList.clear();  // Clear the event list

            // Redirect the user to the login activity after logging out
            Intent intent = new Intent(EventDisplayActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // Close the current activity to prevent going back after logout
        });

        // Set up the SMS Permission button to request or check for SMS permissions
        smsPermissionButton.setOnClickListener(v -> checkAndRequestSmsPermission());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();  // Close the database connection
        }
    }

    /**
     * Method to load all events created by the logged-in user from the database.
     * Events are filtered based on the user ID stored in SharedPreferences.
     */
    private void loadEvents() {
        // Initialize the event list if it's not already done
        if (eventList == null) {
            eventList = new ArrayList<>();
        }

        eventList.clear();  // Clear the list to avoid duplicates

        // Retrieve the user ID from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        long userId = sharedPref.getLong("user_id", -1);  // Default to -1 if no user ID is found

        if (userId != -1) {
            // Query the events from the database using the user ID
            try (Cursor cursor = databaseHelper.getEventsByUserId(userId)) {
                if (cursor != null) {
                    // Iterate through the cursor and add each event to the event list
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow("event_id"));
                        String patientName = cursor.getString(cursor.getColumnIndexOrThrow("patient_name"));
                        String doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctor_name"));
                        String appointmentDate = cursor.getString(cursor.getColumnIndexOrThrow("appointment_date"));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                        String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                        String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                        String pdfUri = cursor.getString(cursor.getColumnIndexOrThrow("pdf_uri"));

                        // Create a new Event object and add it to the event list
                        eventList.add(new Event(id, patientName, doctorName, appointmentDate, status, notes, location, pdfUri));
                    }
                }
            } catch (Exception e) {
                Log.e("EventDisplayActivity", "Error loading events: ", e);  // Log an error message if something goes wrong
            }
        }
    }

    /**
     * Method to open the EventDetailActivity for viewing event details.
     *
     * @param eventId The ID of the event to display.
     */
    private void openEventDetailActivity(long eventId) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);  // Pass the event ID to the detail activity
        startActivity(intent);  // Start the EventDetailActivity
    }

    /**
     * Method to open the AddEventDialogFragment for adding or editing an event.
     * If the event parameter is null, a new event is created; otherwise, the existing event is edited.
     *
     * @param event    The event to be edited (or null for a new event).
     * @param position The position of the event in the list (used for editing).
     */
    public void openAddOrEditEventDialog(Event event, int position) {
        AddEventDialogFragment dialog;
        if (event == null) {
            dialog = new AddEventDialogFragment();  // Create a new event if the event is null
        } else {
            dialog = AddEventDialogFragment.newInstance(event);  // Edit an existing event
        }

        // Set up the listener for saving the event
        dialog.setOnSaveListener(newEvent -> {
            SharedPreferences sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            long userId = sharedPref.getLong("user_id", -1);  // Retrieve the user ID

            if (userId != -1) {
                if (event == null) {
                    // Add a new event to the database
                    long id = databaseHelper.addEvent(
                            newEvent.getPatientName(),
                            newEvent.getDoctorName(),
                            newEvent.getAppointmentDate(),
                            newEvent.getStatus(),
                            newEvent.getNotes(),
                            newEvent.getLocation(),
                            newEvent.getPdfUri(),
                            userId
                    );

                    if (id != -1) {
                        // If the event was added successfully, update the list and adapter
                        newEvent.setId(id);
                        eventList.add(newEvent);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error adding event.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Update an existing event in the database
                    boolean updated = databaseHelper.updateEvent(
                            event.getId(),
                            newEvent.getPatientName(),
                            newEvent.getDoctorName(),
                            newEvent.getAppointmentDate(),
                            newEvent.getStatus(),
                            newEvent.getNotes(),
                            newEvent.getLocation(),
                            newEvent.getPdfUri()
                    );

                    if (updated) {
                        // If the event was updated successfully, update the list and adapter
                        eventList.set(position, newEvent);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Display an error message if the user ID is not found
                Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the Add/Edit Event dialog
        dialog.show(getSupportFragmentManager(), "AddOrEditEventDialog");
    }

    /**
     * Method to show a confirmation dialog before deleting an event.
     *
     * @param position The position of the event in the list that needs to be deleted.
     */
    public void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    long eventId = eventList.get(position).getId();
                    boolean deleted = databaseHelper.deleteEvent(eventId);  // Delete the event from the database

                    if (deleted) {
                        // If deletion is successful, remove the event from the list and notify the adapter
                        eventList.remove(position);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)  // Do nothing if user cancels
                .show();  // Show the confirmation dialog
    }

    /**
     * Method to check if SMS permissions are granted. If not, request the permissions.
     */
    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "SMS permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the result of the SMS permission request.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
