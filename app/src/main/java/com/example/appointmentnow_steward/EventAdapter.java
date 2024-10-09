/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    EventAdapter.java

    This class provides a custom adapter to populate and manage a list of `Event` objects in a
    `ListView` or `GridView` within the AppointmentNow application. The adapter facilitates the
    binding of event data (e.g., patient name, doctor name, status) to individual views (e.g.,
    TextViews, ImageButtons) in the list, allowing for dynamic event management.

    Key Features:
    - Displays a list of events with key details such as patient name, doctor name, and event status.
    - Provides functionality to edit and delete events from the EventDisplayActivity or HistoryActivity.
    - Implements click listeners for each event item to open detailed views and handle user actions.

    Adapter Overview:
    - Extends `BaseAdapter`, a common adapter class in Android used to bind data to a `ListView` or
      `GridView`.
    - Uses the `getView()` method to define how each row of the list should be displayed.
    - Supports event editing and deletion through interaction with the corresponding Activity (EventDisplayActivity, HistoryActivity).

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

public class EventAdapter extends BaseAdapter {
    private final Context context;            // The context of the calling Activity or Fragment
    private final ArrayList<Event> events;    // The list of events to be displayed

    // Constructor to initialize the adapter with context and event list
    public EventAdapter(Context context, ArrayList<Event> events) {
        this.context = context;               // Set the context (either EventDisplayActivity or HistoryActivity)
        this.events = events;                 // Set the list of events to display in the ListView
    }

    // Returns the total number of events
    @Override
    public int getCount() {
        return events.size();                 // Return the size of the event list
    }

    // Returns the event object at a specific position in the list
    @Override
    public Object getItem(int position) {
        return events.get(position);          // Return the event at the specified position
    }

    // Returns the row ID for the specific position (in this case, just the position itself)
    @Override
    public long getItemId(int position) {
        return position;                      // Return the position as the row ID
    }

    // Inflates and populates the view for each row in the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If there is no reusable view, inflate a new one using event_item.xml layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        }

        // Get the event at the current position in the list
        Event event = (Event) getItem(position);

        // Initialize UI components for the list item view (TextViews and ImageButtons)
        TextView eventTitle = convertView.findViewById(R.id.event_title);            // Display patient's name as title
        TextView eventSubtitle = convertView.findViewById(R.id.event_subtitle);      // Display doctor's name as subtitle
        TextView eventStatus = convertView.findViewById(R.id.event_status);          // Display the event's status
        ImageButton editButton = convertView.findViewById(R.id.edit_event_button);   // Edit button for each event
        ImageButton deleteButton = convertView.findViewById(R.id.delete_event_button); // Delete button for each event

        // Set data for each UI component (Patient Name, Doctor Name, Status)
        eventTitle.setText(event.getPatientName());         // Set the patient's name as the title
        eventSubtitle.setText(event.getDoctorName());       // Set the doctor's name as the subtitle
        eventStatus.setText(event.getStatus());             // Set the event status (e.g., Scheduled, Completed)

        // Set a click listener on the entire view to open EventDetailActivity when the item is clicked
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);          // Create an intent for EventDetailActivity
            intent.putExtra("event_id", event.getId());                              // Pass the event ID to the detail activity
            context.startActivity(intent);                                           // Start the detail activity
        });

        // Edit event functionality: opens the dialog for editing the event based on the context
        editButton.setOnClickListener(v -> {
            if (context instanceof EventDisplayActivity) {
                ((EventDisplayActivity) context).openAddOrEditEventDialog(event, position); // Open edit dialog in EventDisplayActivity
            } else if (context instanceof HistoryActivity) {
                ((HistoryActivity) context).openEditEventDialog(event, position);         // Open edit dialog in HistoryActivity
            }
        });

        // Delete event functionality: shows a delete confirmation dialog based on the context
        deleteButton.setOnClickListener(v -> {
            if (context instanceof EventDisplayActivity) {
                ((EventDisplayActivity) context).showDeleteConfirmationDialog(position);   // Show delete dialog in EventDisplayActivity
            } else if (context instanceof HistoryActivity) {
                ((HistoryActivity) context).showDeleteConfirmationDialog(event.getId(), position); // Show delete dialog in HistoryActivity
            }
        });

        return convertView; // Return the fully constructed view for this row
    }
}
