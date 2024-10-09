/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    Event.java

    This class represents an appointment event in the AppointmentNow application. It implements the
    `Parcelable` interface, which allows instances of this class to be passed between Android
    components through Intents or Bundles. Each event stores relevant information such as the
    patient's name, doctor's name, appointment date, status, notes, and location.

    Key Features:
    - Implements `Parcelable` to allow the Event object to be passed between Android components.
    - Provides constructors to create Event objects with and without an ID (for new and existing events).
    - Stores appointment details such as patient name, doctor name, appointment date, status,
      notes, and location.
    - Includes getter and setter methods for each property, enabling easy access and modification.

    Parcelable Interface:
    - Parcelable is an Android-specific interface used to serialize and deserialize objects, allowing
      them to be passed between activities, services, or fragments.
    - The `writeToParcel()` method serializes the object's data, while the `CREATOR` object and
      `createFromParcel()` method deserialize the data to reconstruct the object.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {

    // Fields to store the event details
    private long id;                 // Unique identifier for the event (used when stored in a database)
    private String patientName;      // The patient's name
    private String doctorName;       // The doctor's name
    private String appointmentDate;  // Date of the appointment
    private String status;           // Status of the appointment (e.g., Scheduled, Completed)
    private String notes;            // Additional notes related to the appointment
    private String location;         // Location of the appointment
    private String pdfUri;           // URI of an optional PDF associated with the event

    /**
     * Constructor with an ID. This is used for events that already exist in the database and
     * have an ID associated with them.
     *
     * @param id              The unique ID of the event.
     * @param patientName     The name of the patient.
     * @param doctorName      The name of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param status          The status of the appointment (e.g., Scheduled, Completed).
     * @param notes           Additional notes about the appointment.
     * @param location        The location of the appointment.
     * @param pdfUri          The URI of the PDF file, if any.
     */
    public Event(long id, String patientName, String doctorName, String appointmentDate, String status, String notes, String location, String pdfUri) {
        this.id = id;  // Set the ID for an existing event
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
        this.location = location;
        this.pdfUri = pdfUri;  // Set the PDF URI (optional)
    }

    /**
     * Constructor without an ID. This is used for newly created events that do not yet have
     * an ID in the database (e.g., when the user is creating a new appointment).
     *
     * @param patientName     The name of the patient.
     * @param doctorName      The name of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param status          The status of the appointment (e.g., Scheduled, Completed).
     * @param notes           Additional notes about the appointment.
     * @param location        The location of the appointment.
     * @param pdfUri          The URI of the PDF file, if any.
     */
    public Event(String patientName, String doctorName, String appointmentDate, String status, String notes, String location, String pdfUri) {
        // No ID is assigned yet since this is for a new event
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
        this.location = location;
        this.pdfUri = pdfUri;  // Set the PDF URI (optional)
    }

    /**
     * Constructor used by the Parcelable interface to recreate an Event object from a Parcel.
     * This is needed for passing Event objects between Android components (e.g., Activities).
     *
     * @param in The Parcel containing the serialized Event data.
     */
    protected Event(Parcel in) {
        id = in.readLong();  // Read the event ID from the Parcel
        patientName = in.readString();  // Read the patient name
        doctorName = in.readString();   // Read the doctor name
        appointmentDate = in.readString();  // Read the appointment date
        status = in.readString();  // Read the appointment status
        notes = in.readString();  // Read the appointment notes
        location = in.readString();  // Read the appointment location
        pdfUri = in.readString();  // Read the PDF URI (can be null)
    }

    /**
     * Method required by the Parcelable interface to serialize the Event object's data into a Parcel.
     * This allows the Event object to be passed between Android components.
     *
     * @param dest  The Parcel where the object's data should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);  // Write the event ID
        dest.writeString(patientName);  // Write the patient name
        dest.writeString(doctorName);   // Write the doctor name
        dest.writeString(appointmentDate);  // Write the appointment date
        dest.writeString(status);  // Write the appointment status
        dest.writeString(notes);  // Write the notes
        dest.writeString(location);  // Write the location
        dest.writeString(pdfUri);  // Write the PDF URI (can be null)
    }

    /**
     * Method required by the Parcelable interface, but not used in this case.
     * It describes any special objects contained in the Parcelable.
     *
     * @return 0 because there are no special objects in this class.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    // CREATOR field used to generate instances of the Event class from a Parcel
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);  // Calls the constructor that reads from the Parcel
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];  // Creates an array of Event objects
        }
    };

    // Getters and Setters for the Event properties

    public long getId() {
        return id;  // Get the event ID
    }

    public void setId(long id) {
        this.id = id;  // Set the event ID (used when saving to the database)
    }

    public String getPatientName() {
        return patientName;  // Get the patient name
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;  // Set the patient name
    }

    public String getDoctorName() {
        return doctorName;  // Get the doctor name
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;  // Set the doctor name
    }

    public String getAppointmentDate() {
        return appointmentDate;  // Get the appointment date
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;  // Set the appointment date
    }

    public String getStatus() {
        return status;  // Get the appointment status
    }

    public void setStatus(String status) {
        this.status = status;  // Set the appointment status
    }

    public String getNotes() {
        return notes;  // Get any additional notes
    }

    public void setNotes(String notes) {
        this.notes = notes;  // Set any additional notes
    }

    public String getLocation() {
        return location;  // Get the location of the appointment
    }

    public void setLocation(String location) {
        this.location = location;  // Set the location of the appointment
    }

    public String getPdfUri() {
        return pdfUri;  // Get the PDF URI
    }

    public void setPdfUri(String pdfUri) {
        this.pdfUri = pdfUri;  // Set the PDF URI (optional)
    }
}
