/*
 *     Appointment Now - History Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity displays a list of completed events for the logged-in user.
 *     Users can view, edit, delete, and search for past appointments.
 * 
 *     Features:
 *     - Displays completed events in a GridView.
 *     - Allows users to edit or delete events.
 *     - Supports search functionality to filter past events by patient name.
 * 
 *     Dependencies:
 *     - DatabaseHelper.java (Manages SQLite interactions)
 *     - EventAdapter.java (Binds event data to UI)
 *     - AddEventDialogFragment.java (Handles event editing)
 * 
 *     Issues:
 *     - History button is currently disabled due to a recent update. Needs fixing.
 */

package com.example.appointmentnow_steward;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "HistoryActivity";
    private static final String PREF_NAME = "AppPreferences";
    private static final String USER_ID_KEY = "user_id";

    // UI Components
    private GridView completedEventsGrid;
    private EditText searchEditText;
    private List<Event> completedEventsList;
    private EventAdapter eventAdapter;
    private DatabaseHelper databaseHelper;

    // User ID for session management
    private long userId;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize database helper and retrieve user ID once
        databaseHelper = new DatabaseHelper(this);
        userId = getUserId();

        if (userId == -1) {
            showErrorAndExit("Error loading history. Please log in again.");
            return;
        }

        initializeUI();
        loadCompletedEvents("");  // Initial load with no filter
    }

    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initializeUI() {
        completedEventsGrid = findViewById(R.id.completed_events_grid);
        searchEditText = findViewById(R.id.search_edit_text);
        ImageButton closeButton = findViewById(R.id.close_button);

        // Initialize event list and adapter
        completedEventsList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, completedEventsList);
        completedEventsGrid.setAdapter(eventAdapter);

        // Close button exits the activity
        closeButton.setOnClickListener(v -> finish());

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCompletedEvents(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads completed events from the database.
     *
     * @param searchTerm The search term for filtering completed events.
     */
    private void loadCompletedEvents(String searchTerm) {
        completedEventsList.clear();  // Clear previous data before adding new

        try (Cursor cursor = databaseHelper.getEventsByStatus(userId, "Completed", searchTerm)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    completedEventsList.add(new Event(
                            cursor.getLong(cursor.getColumnIndexOrThrow("event_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patient_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("doctor_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("appointment_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status")),
                            cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                            cursor.getString(cursor.getColumnIndexOrThrow("location")),
                            cursor.getString(cursor.getColumnIndexOrThrow("pdf_uri"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        eventAdapter.notifyDataSetChanged();
    }

    /**
     * Opens the edit event dialog.
     *
     * @param event    The event to be edited.
     * @param position The position of the event in the list.
     */
    public void openEditEventDialog(Event event, int position) {
        AddEventDialogFragment dialog = AddEventDialogFragment.newInstance(event);
        dialog.setOnSaveListener(updatedEvent -> {
            boolean updated = databaseHelper.updateEvent(
                    event.getId(),
                    updatedEvent.getPatientName(),
                    updatedEvent.getDoctorName(),
                    updatedEvent.getAppointmentDate(),
                    updatedEvent.getStatus(),
                    updatedEvent.getNotes(),
                    updatedEvent.getLocation(),
                    updatedEvent.getPdfUri()
            );

            if (updated) {
                completedEventsList.set(position, updatedEvent);
                eventAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getSupportFragmentManager(), "EditEventDialog");
    }

    /**
     * Displays a confirmation dialog before deleting an event.
     *
     * @param eventId  The ID of the event to be deleted.
     * @param position The position of the event in the list.
     */
    public void showDeleteConfirmationDialog(long eventId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteEvent(eventId);
                    if (deleted) {
                        completedEventsList.remove(position);
                        eventAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Retrieves the logged-in user's ID.
     */
    private long getUserId() {
        return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getLong(USER_ID_KEY, -1);
    }

    /**
     * Displays an error message and exits the activity.
     */
    private void showErrorAndExit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
