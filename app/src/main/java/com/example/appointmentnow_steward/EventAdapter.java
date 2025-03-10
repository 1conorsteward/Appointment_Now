/*
 *     Appointment Now - Event Adapter (RecyclerView)
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This class provides a RecyclerView adapter for displaying a list of `Event` objects 
 *     in the AppointmentNow application.
 *     
 *     Features:
 *     - Uses RecyclerView for better performance over ListView.
 *     - Implements ViewHolder pattern to minimize redundant view binding.
 *     - Supports event editing and deletion via buttons.
 *     - Implements click listeners for opening event details.
 * 
 *     Dependencies:
 *     - `Event.java` (Event model class)
 *     - `EventDisplayActivity.java`, `HistoryActivity.java` (Managing UI interactions)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;   // The context of the calling Activity or Fragment
    private final List<Event> events; // List of events to display

    /**
     * Constructor for initializing the adapter with context and event list.
     *
     * @param context The calling activity or fragment.
     * @param events  The list of events to be displayed.
     */
    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    /**
     * Inflates the event item layout when the ViewHolder is created.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder for each event in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        // Set event details
        holder.eventTitle.setText(event.getPatientName());
        holder.eventSubtitle.setText(event.getDoctorName());
        holder.eventStatus.setText(event.getStatus());

        // Set up event click listeners
        holder.itemView.setOnClickListener(v -> openEventDetails(event));
        holder.editButton.setOnClickListener(v -> openEditEventDialog(event, position));
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmation(event.getId(), position));
    }

    /**
     * Returns the total number of events.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Opens EventDetailActivity to show details of the selected event.
     */
    private void openEventDetails(Event event) {
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        context.startActivity(intent);
    }

    /**
     * Handles event editing based on the calling context.
     */
    private void openEditEventDialog(Event event, int position) {
        if (context instanceof EventDisplayActivity) {
            ((EventDisplayActivity) context).openAddOrEditEventDialog(event, position);
        } else if (context instanceof HistoryActivity) {
            ((HistoryActivity) context).openEditEventDialog(event, position);
        }
    }

    /**
     * Shows a delete confirmation dialog based on the calling context.
     */
    private void showDeleteConfirmation(long eventId, int position) {
        if (context instanceof EventDisplayActivity) {
            ((EventDisplayActivity) context).showDeleteConfirmationDialog(position);
        } else if (context instanceof HistoryActivity) {
            ((HistoryActivity) context).showDeleteConfirmationDialog(eventId, position);
        }
    }

    /**
     * ViewHolder pattern for efficient view reuse.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView eventTitle, eventSubtitle, eventStatus;
        final ImageButton editButton, deleteButton;

        /**
         * Constructor that binds UI components from the event_item layout.
         */
        EventViewHolder(View view) {
            super(view);
            eventTitle = view.findViewById(R.id.event_title);
            eventSubtitle = view.findViewById(R.id.event_subtitle);
            eventStatus = view.findViewById(R.id.event_status);
            editButton = view.findViewById(R.id.edit_event_button);
            deleteButton = view.findViewById(R.id.delete_event_button);
        }
    }
}
