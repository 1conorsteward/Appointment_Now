/*
 *     Appointment Now - Add Event Dialog
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This DialogFragment allows users to add or edit an appointment event in the AppointmentNow application.
 *     Users can input details such as patient name, doctor name, date, status, notes, and location.
 *     Additionally, users can attach a PDF file related to the appointment.
 * 
 *     Features:
 *     - Displays a form for creating or editing events.
 *     - Supports selecting a date using DatePickerDialog.
 *     - Allows users to upload a PDF document.
 *     - Saves event details to an SQLite database.
 *     - Notifies the parent activity of changes via an interface callback.
 * 
 *     Dependencies:
 *     - AndroidX Fragments, Dialogs, and Lifecycle Components
 *     - SQLite Database Helper (`DatabaseHelper.java`)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

    // UI Components
    private EditText patientName, doctorName, appointmentDate, appointmentNotes, appointmentLocation;
    private Spinner appointmentStatus;
    private Button saveEventButton, addPdfButton;
    private ImageButton closeButton;

    // Supporting Variables
    private Calendar calendar;
    private Event event;
    private DatabaseHelper databaseHelper;
    private Uri pdfUri;  // Stores URI of selected PDF file

    // Constants
    private static final String DATE_FORMAT = "yyyy-MM-dd";  // Expected date format
    private static final String EVENT_KEY = "event";  // Key for passing event data between fragments
    private static final String TAG = "AddEventDialogFragment";  // Log tag for debugging

    /**
     * Interface for communicating the save action to the parent activity or fragment.
     */
    public interface OnSaveListener {
        void onSave(Event event);
    }

    private OnSaveListener onSaveListener;

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    /**
     * Creates a new instance of AddEventDialogFragment for editing an existing event.
     *
     * @param event The event object to be edited (null if creating a new event).
     * @return A new instance of AddEventDialogFragment.
     */
    public static AddEventDialogFragment newInstance(Event event) {
        AddEventDialogFragment fragment = new AddEventDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EVENT_KEY, event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the dialog view and initializes UI components.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_event, container, false);

        initializeUI(view);
        setupEventHandlers();

        // If an event is passed for editing, populate the fields
        if (getArguments() != null) {
            event = getArguments().getParcelable(EVENT_KEY);
            if (event != null) populateFields();
        }

        return view;
    }

    /**
     * Initializes UI components for the dialog.
     */
    private void initializeUI(View view) {
        patientName = view.findViewById(R.id.patient_name);
        doctorName = view.findViewById(R.id.doctor_name);
        appointmentDate = view.findViewById(R.id.appointment_date);
        appointmentStatus = view.findViewById(R.id.appointment_status);
        appointmentNotes = view.findViewById(R.id.appointment_notes);
        appointmentLocation = view.findViewById(R.id.appointment_location);
        saveEventButton = view.findViewById(R.id.save_event_button);
        closeButton = view.findViewById(R.id.close_button);
        addPdfButton = view.findViewById(R.id.button_add_pdf);

        calendar = Calendar.getInstance();  // Initialize calendar for date picker
        databaseHelper = new DatabaseHelper(getContext());  // Initialize database helper
    }

    /**
     * Sets up event handlers for UI interactions.
     */
    private void setupEventHandlers() {
        closeButton.setOnClickListener(v -> dismiss());  // Close dialog when close button is clicked

        // Open Date Picker when the appointment date field is clicked
        appointmentDate.setOnClickListener(v -> new DatePickerDialog(
                requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        );

        saveEventButton.setOnClickListener(v -> handleSaveEvent());  // Save event when save button is clicked

        // PDF Picker using AndroidX ActivityResultLauncher
        ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        pdfUri = result.getData().getData();  // Store selected PDF URI
                        Log.d(TAG, "Selected PDF URI: " + pdfUri);
                    }
                }
        );

        // Open file picker to select a PDF
        addPdfButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pdfPickerLauncher.launch(intent);
        });
    }

    /**
     * Populates input fields with existing event data when editing.
     */
    private void populateFields() {
        patientName.setText(event.getPatientName());
        doctorName.setText(event.getDoctorName());
        appointmentDate.setText(event.getAppointmentDate());
        setSpinnerSelectionByValue(appointmentStatus, event.getStatus());
        appointmentNotes.setText(event.getNotes());
        appointmentLocation.setText(event.getLocation());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            calendar.setTime(Objects.requireNonNull(sdf.parse(event.getAppointmentDate())));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage(), e);
        }

        pdfUri = event.getPdfUri() != null ? Uri.parse(event.getPdfUri()) : null;
    }

    /**
     * Saves the new or updated event to the database.
     */
    private void handleSaveEvent() {
        if (!validateInputs()) return;

        long userId = getUserId();
        if (userId == -1) return;

        boolean success = event == null ? 
                databaseHelper.addEvent(getInputText(patientName), getInputText(doctorName),
                        getInputText(appointmentDate), getSelectedSpinnerValue(appointmentStatus),
                        getInputText(appointmentNotes), getInputText(appointmentLocation),
                        pdfUri != null ? pdfUri.toString() : null, userId) != -1
                : databaseHelper.updateEvent(event.getId(), getInputText(patientName), getInputText(doctorName),
                        getInputText(appointmentDate), getSelectedSpinnerValue(appointmentStatus),
                        getInputText(appointmentNotes), getInputText(appointmentLocation),
                        pdfUri != null ? pdfUri.toString() : null);

        showToast(success ? "Event saved successfully!" : "Error saving event.");
        if (success && onSaveListener != null) onSaveListener.onSave(event);
        dismiss();
    }
}
