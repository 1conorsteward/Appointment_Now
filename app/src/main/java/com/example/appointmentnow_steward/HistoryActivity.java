/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    HistoryActivity.java

    This class represents the history screen in the AppointmentNow application where users can view, edit, and delete
    all completed events (appointments). Users can search for specific events using a search bar, and they can modify
    or remove events as needed. This activity interfaces with the database to load completed events associated with
    the logged-in user.

    Key Features:
    - Displays completed events in a grid view format.
    - Allows users to edit or delete completed events.
    - Includes a search feature to filter completed events based on the patient's name.
    - Supports closing the activity using a close button.

    Issues: A most recent update has disabled the history button, will need to solve this soon.
*/

package com.example.appointmentnow_steward;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    // List to store completed events
    private ArrayList<Event> completedEventsList;

    // Database helper for accessing the SQLite database
    private DatabaseHelper databaseHelper;

    // EventAdapter to bind completed events to the GridView
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);  // Set the layout for the history screen

        // Initialize close button and set click listener
        ImageButton closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());  // Close the activity when the close button is clicked

        // Initialize the UI components
        GridView completedEventsGrid = findViewById(R.id.completed_events_grid);  // GridView to display completed events
        EditText searchEditText = findViewById(R.id.search_edit_text);  // EditText for searching completed events

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);

        // Load completed events from the database (initial load with no search filter)
        loadCompletedEvents("");

        // Initialize and set up the event adapter for the GridView using the class-level variable
        eventAdapter = new EventAdapter(this, completedEventsList);  // Bind the list of completed events to the adapter
        completedEventsGrid.setAdapter(eventAdapter);  // Set the adapter to the GridView

        // Set up the search functionality to filter completed events based on the search input
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Reload the list based on search input, dynamically updating the results
                loadCompletedEvents(s.toString());  // Filter the events by the search term
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    // Method to open edit event dialog
    public void openEditEventDialog(Event event, int position) {
        AddEventDialogFragment dialog = AddEventDialogFragment.newInstance(event);  // Create a dialog for editing the event
        dialog.setOnSaveListener(updatedEvent -> {
            // Update the event in the database with the new information
            boolean updated = databaseHelper.updateEvent(
                    event.getId(),
                    updatedEvent.getPatientName(),
                    updatedEvent.getDoctorName(),
                    updatedEvent.getAppointmentDate(),
                    updatedEvent.getStatus(),
                    updatedEvent.getNotes(),
                    updatedEvent.getLocation(),
                    updatedEvent.getPdfUri());

            // Refresh the list if the event was updated successfully
            if (updated) {
                completedEventsList.set(position, updatedEvent);  // Update the event in the list
                eventAdapter.notifyDataSetChanged();  // Refresh the adapter to reflect the updated data
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();  // Display an error message if the update fails
            }
        });

        // Show the dialog to edit the event
        dialog.show(getSupportFragmentManager(), "EditEventDialog");
    }

    // Method to show delete confirmation dialog
    public void showDeleteConfirmationDialog(long eventId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")  // Prompt the user for confirmation
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the event from the database
                    boolean deleted = databaseHelper.deleteEvent(eventId);

                    // Remove the event from the list and refresh the adapter if deletion is successful
                    if (deleted) {
                        completedEventsList.remove(position);  // Remove the event from the list
                        eventAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
                        Toast.makeText(this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting event.", Toast.LENGTH_SHORT).show();  // Display an error message if deletion fails
                    }
                })
                .setNegativeButton("Cancel", null)  // Do nothing if the user cancels
                .show();  // Show the confirmation dialog
    }

    // Method to load completed events from the database
    private void loadCompletedEvents(String searchTerm) {
        // Initialize the list if it's not already done
        if (completedEventsList == null) {
            completedEventsList = new ArrayList<>();
        }

        completedEventsList.clear();  // Clear the list to avoid duplicates

        // Retrieve the logged-in user ID from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        long userId = sharedPref.getLong("user_id", -1);  // Default to -1 if the user ID is not found

        if (userId != -1) {
            // Query completed events from the database using the user ID and search term
            Cursor cursor = databaseHelper.getCompletedEvents(userId, searchTerm);  // Filter events by the search term
            if (cursor != null) {
                try {
                    // Iterate through the cursor to retrieve event details
                    int idColumnIndex = cursor.getColumnIndexOrThrow("event_id");
                    int patientNameColumnIndex = cursor.getColumnIndexOrThrow("patient_name");
                    int doctorNameColumnIndex = cursor.getColumnIndexOrThrow("doctor_name");
                    int appointmentDateColumnIndex = cursor.getColumnIndexOrThrow("appointment_date");
                    int statusColumnIndex = cursor.getColumnIndexOrThrow("status");
                    int notesColumnIndex = cursor.getColumnIndexOrThrow("notes");
                    int locationColumnIndex = cursor.getColumnIndexOrThrow("location");
                    int pdfUriColumnIndex = cursor.getColumnIndexOrThrow("pdf_uri");

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumnIndex);
                        String patientName = cursor.getString(patientNameColumnIndex);
                        String doctorName = cursor.getString(doctorNameColumnIndex);
                        String appointmentDate = cursor.getString(appointmentDateColumnIndex);
                        String status = cursor.getString(statusColumnIndex);
                        String notes = cursor.getString(notesColumnIndex);
                        String location = cursor.getString(locationColumnIndex);
                        String pdfUri = cursor.getString(pdfUriColumnIndex);

                        // Add the event to the list of completed events
                        completedEventsList.add(new Event(id, patientName, doctorName, appointmentDate, status, notes, location, pdfUri));
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    cursor.close();  // Close the cursor after processing
                }
            }
        } else {
            // Display an error message if the user ID is not found
            Toast.makeText(this, "Error loading history. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }
}
