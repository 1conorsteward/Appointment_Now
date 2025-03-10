/*
 *     Appointment Now - Event Display Activity
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This activity displays all events (appointments) for the logged-in user.
 *     Users can add, update, delete, and view event details.
 *     
 *     Features:
 *     - Loads events from SQLite and displays them in a RecyclerView.
 *     - Allows users to add new events via AddEventDialogFragment.
 *     - Supports viewing detailed event info in EventDetailActivity.
 *     - Manages user logout and session clearing.
 * 
 *     Dependencies:
 *     - `DatabaseHelper.java` (Manages SQLite interactions)
 *     - `EventAdapter.java` (Binds event data to UI)
 *     - `AddEventDialogFragment.java` (Handles event creation and editing)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventDisplayActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "EventDisplayActivity";
    private static final String PREF_NAME = "AppPreferences";
    private static final String USER_ID_KEY = "user_id";

    // UI Components
    private RecyclerView eventRecyclerView;
    private List<Event> eventList;
    private EventAdapter eventAdapter;
    private DatabaseHelper databaseHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_display);

        initializeUI();
        databaseHelper = new DatabaseHelper(this);
        loadEventsAsync();
    }

    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initializeUI() {
        eventRecyclerView = findViewById(R.id.event_recycler_view);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addEventButton = findViewById(R.id.add_event_button);
        ImageButton logoutButton = findViewById(R.id.action_logout);

        // Event List Setup
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList);
        eventRecyclerView.setAdapter(eventAdapter);

        // Event Listeners
        addEventButton.setOnClickListener(v -> openAddOrEditEventDialog(null, -1));
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    /**
     * Loads all events asynchronously to prevent UI blocking.
     */
    private void loadEventsAsync() {
        new LoadEventsTask().execute();
    }

    /**
     * AsyncTask to load events in the background.
     */
    private class LoadEventsTask extends AsyncTask<Void, Void, List<Event>> {
        @Override
        protected List<Event> doInBackground(Void... voids) {
            return fetchEventsFromDatabase();
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            } else {
                showErrorAndExit("Failed to load events.");
            }
        }
    }

    /**
     * Fetches all events created by the logged-in user from the database.
     */
    private List<Event> fetchEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
        long userId = getUserId();
        if (userId == -1) return null;

        try (Cursor cursor = databaseHelper.getEventsByUserId(userId)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    events.add(new Event(
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
            Log.e(TAG, "Error loading events", e);
            return null;
        }
        return events;
    }

    /**
     * Opens the add/edit event dialog.
     */
    public void openAddOrEditEventDialog(Event event, int position) {
        AddEventDialogFragment dialog = (event == null) ? new AddEventDialogFragment() : AddEventDialogFragment.newInstance(event);

        dialog.setOnSaveListener(newEvent -> {
            long userId = getUserId();
            if (userId == -1) {
                showErrorAndExit("User ID not found. Please log in again.");
                return;
            }

            boolean success;
            if (event == null) {
                long id = databaseHelper.addEvent(
                        newEvent.getPatientName(), newEvent.getDoctorName(), newEvent.getAppointmentDate(),
                        newEvent.getStatus(), newEvent.getNotes(), newEvent.getLocation(), newEvent.getPdfUri(), userId
                );
                success = (id != -1);
                if (success) {
                    newEvent.setId(id);
                    eventList.add(newEvent);
                }
            } else {
                success = databaseHelper.updateEvent(
                        event.getId(), newEvent.getPatientName(), newEvent.getDoctorName(), newEvent.getAppointmentDate(),
                        newEvent.getStatus(), newEvent.getNotes(), newEvent.getLocation(), newEvent.getPdfUri()
                );
                if (success) {
                    eventList.set(position, newEvent);
                }
            }

            if (success) {
                eventAdapter.notifyDataSetChanged();
                Toast.makeText(this, event == null ? "Event added successfully!" : "Event updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving event.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getSupportFragmentManager(), "AddOrEditEventDialog");
    }

    /**
     * Logs out the user and clears session data.
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.clear().apply();

        eventList.clear();
        startActivity(new Intent(EventDisplayActivity.this, LoginActivity.class));
        finish();
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