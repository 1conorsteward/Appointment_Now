/*
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Version: 2.1
 *
 * AddEventDialogFragment.java
 * This fragment represents the dialog for adding or editing an event (appointment) in the AppointmentNow application.
 * Users can input the patient's name, doctor's name, appointment date, status, notes, and location.
 * Users can also upload a PDF document associated with the event.
 * The dialog allows users to either create a new event or edit an existing event.
 *
 * Issues: No known issues
 */

package com.example.appointmentnow_steward;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddEventDialogFragment extends DialogFragment {

    // Declare UI components for user input
    private EditText patientName;
    private EditText doctorName;
    private EditText appointmentDate;
    private Spinner appointmentStatus;
    private EditText appointmentNotes;
    private EditText appointmentLocation;
    private Calendar calendar;

    private Event event; // Event object to store the details of the event
    private DatabaseHelper databaseHelper; // DatabaseHelper for database operations

    // Declare PDF selection variables
    private Uri pdfUri; // URI for the uploaded PDF document

    // Interface for communicating the save action to the parent activity/fragment
    public interface OnSaveListener {
        void onSave(Event event); // Method to handle the save event
    }

    private OnSaveListener onSaveListener; // Listener for save event

    /**
     * Method to set the save listener for the dialog fragment.
     *
     * @param onSaveListener The listener to handle the save event
     */
    public void setOnSaveListener(OnSaveListener onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    /**
     * Static method to create a new instance of AddEventDialogFragment for editing an event.
     *
     * @param event The event to be edited.
     * @return A new instance of AddEventDialogFragment with the event data.
     */
    public static AddEventDialogFragment newInstance(Event event) {
        AddEventDialogFragment fragment = new AddEventDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", event); // Store event data in arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_event, container, false);

        // Initialize UI components
        patientName = view.findViewById(R.id.patient_name);
        doctorName = view.findViewById(R.id.doctor_name);
        appointmentDate = view.findViewById(R.id.appointment_date);
        appointmentStatus = view.findViewById(R.id.appointment_status);
        appointmentNotes = view.findViewById(R.id.appointment_notes);
        appointmentLocation = view.findViewById(R.id.appointment_location);
        Button saveEventButton = view.findViewById(R.id.save_event_button);
        ImageButton closeButton = view.findViewById(R.id.close_button);
        Button addPdfButton = view.findViewById(R.id.button_add_pdf); // Add PDF button

        // Initialize calendar for date picker
        calendar = Calendar.getInstance();
        databaseHelper = new DatabaseHelper(getContext()); // Initialize database helper

        // Close button dismisses the dialog
        closeButton.setOnClickListener(v -> dismiss());

        // Set up DatePickerDialog for appointment date input
        appointmentDate.setOnClickListener(v -> new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());

        // Register for PDF picking result
        ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            pdfUri = data.getData(); // Get the selected PDF URI
                            Log.d("PDF_URI", "Selected PDF URI: " + pdfUri);
                        }
                    }
                }
        );

        // Handle Add PDF button click
        addPdfButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pdfPickerLauncher.launch(intent); // Launch the PDF picker intent
        });

        // Check if we are editing an existing event (received via arguments)
        if (getArguments() != null) {
            event = getArguments().getParcelable("event", Event.class); // Get the event to be edited

            if (event != null) {
                // Populate the input fields with the event data for editing
                patientName.setText(event.getPatientName());
                doctorName.setText(event.getDoctorName());
                appointmentDate.setText(event.getAppointmentDate());
                setSpinnerSelectionByValue(appointmentStatus, event.getStatus());
                appointmentNotes.setText(event.getNotes());
                appointmentLocation.setText(event.getLocation());

                // Parse date and update the calendar object
                try {
                    String myFormat = "yyyy-MM-dd"; // Date format
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    calendar.setTime(Objects.requireNonNull(sdf.parse(event.getAppointmentDate()))); // Set the date in the calendar
                } catch (Exception e) {
                    Log.e("AddEventDialogFragment", "Error parsing date: " + e.getMessage(), e);
                }

                // Set the pdfUri if the event has an associated PDF
                pdfUri = event.getPdfUri() != null ? Uri.parse(event.getPdfUri()) : null;
            }
        }

        // Handle Save button click
        saveEventButton.setOnClickListener(v -> {
            if (validateInputs()) { // Ensure inputs are valid before proceeding
                if (getActivity() != null) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
                    long userId = sharedPref.getLong("user_id", -1); // Get the logged-in user's ID

                    if (userId != -1) {
                        // Save or update the event
                        if (event == null) { // New event
                            long eventId = databaseHelper.addEvent(
                                    patientName.getText().toString(),
                                    doctorName.getText().toString(),
                                    appointmentDate.getText().toString(),
                                    appointmentStatus.getSelectedItem().toString(),
                                    appointmentNotes.getText().toString(),
                                    appointmentLocation.getText().toString(),
                                    pdfUri != null ? pdfUri.toString() : null, // Include PDF URI if available
                                    userId
                            );

                            if (eventId != -1) {
                                Toast.makeText(getContext(), "Event added successfully!", Toast.LENGTH_SHORT).show();
                                if (onSaveListener != null) {
                                    onSaveListener.onSave(createEventFromInputs(eventId)); // Notify the parent activity of the new event
                                }
                                dismiss(); // Close the dialog
                            } else {
                                Toast.makeText(getContext(), "Error adding event.", Toast.LENGTH_SHORT).show();
                            }
                        } else { // Editing an existing event
                            boolean updated = databaseHelper.updateEvent(
                                    event.getId(),
                                    patientName.getText().toString(),
                                    doctorName.getText().toString(),
                                    appointmentDate.getText().toString(),
                                    appointmentStatus.getSelectedItem().toString(),
                                    appointmentNotes.getText().toString(),
                                    appointmentLocation.getText().toString(),
                                    pdfUri != null ? pdfUri.toString() : null
                            );

                            if (updated) {
                                Toast.makeText(getContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                                if (onSaveListener != null) {
                                    onSaveListener.onSave(event); // Notify the parent activity of the updated event
                                }
                                dismiss(); // Close the dialog
                            } else {
                                Toast.makeText(getContext(), "Error updating event.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    /**
     * DateSetListener for handling the date selection in DatePickerDialog.
     */
    private final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel(); // Update the date input field with the selected date
    };

    /**
     * Updates the appointment date EditText with the formatted date.
     */
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; // Define date format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        appointmentDate.setText(sdf.format(calendar.getTime())); // Set formatted date
    }

    /**
     * Validates user inputs for adding or editing an event.
     *
     * @return True if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        if (patientName.getText().toString().trim().isEmpty()) {
            patientName.setError("Patient name is required");
            return false;
        }

        if (doctorName.getText().toString().trim().isEmpty()) {
            doctorName.setError("Doctor name is required");
            return false;
        }

        if (appointmentDate.getText().toString().trim().isEmpty()) {
            appointmentDate.setError("Appointment date is required");
            return false;
        }

        return true; // All inputs are valid
    }

    /**
     * Creates an Event object from the input fields.
     *
     * @param id The event ID (for existing events).
     * @return The created Event object.
     */
    private Event createEventFromInputs(long id) {
        if (event == null) { // Create a new event if not editing an existing one
            event = new Event(
                    id, // Pass the event ID
                    patientName.getText().toString(),
                    doctorName.getText().toString(),
                    appointmentDate.getText().toString(),
                    appointmentStatus.getSelectedItem().toString(),
                    appointmentNotes.getText().toString(),
                    appointmentLocation.getText().toString(),
                    pdfUri != null ? pdfUri.toString() : null // Include PDF URI if available
            );
        } else {
            // Update existing event
            event.setPatientName(patientName.getText().toString());
            event.setDoctorName(doctorName.getText().toString());
            event.setAppointmentDate(appointmentDate.getText().toString());
            event.setStatus(appointmentStatus.getSelectedItem().toString());
            event.setNotes(appointmentNotes.getText().toString());
            event.setLocation(appointmentLocation.getText().toString());
            event.setPdfUri(pdfUri != null ? pdfUri.toString() : null); // Update PDF URI if available
        }
        return event; // Return the created or updated event
    }

    /**
     * Helper method to set the spinner selection based on the status value.
     *
     * @param spinner The spinner to be set.
     * @param value   The value to select in the spinner.
     */
    private void setSpinnerSelectionByValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
