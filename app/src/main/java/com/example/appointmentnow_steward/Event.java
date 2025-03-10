/*
 *     Appointment Now - Event Model
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This class represents an appointment event in the AppointmentNow application. 
 *     It implements the `Parcelable` interface, allowing instances to be passed between 
 *     Android components via Intents or Bundles.
 *     
 *     Features:
 *     - Implements `Parcelable` for efficient object serialization and transfer.
 *     - Provides constructors for both new and existing events.
 *     - Encapsulates appointment details such as patient, doctor, date, status, notes, and location.
 *     - Ensures proper handling of optional fields (e.g., `pdfUri` may be null).
 * 
 *     Dependencies:
 *     - Android Parcelable Interface (`android.os.Parcelable`)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {

    // -------------------- Fields (Event Attributes) --------------------
    private long id;                 // Unique event ID (used for database storage)
    private String patientName;      // Patient's name
    private String doctorName;       // Doctor's name
    private String appointmentDate;  // Date of the appointment
    private String status;           // Status of the appointment (e.g., Scheduled, Completed)
    private String notes;            // Additional notes about the appointment
    private String location;         // Location of the appointment
    private String pdfUri;           // URI of an attached PDF file (optional)

    // -------------------- Constructors --------------------

    /**
     * Constructor for an existing event (with ID).
     * Used for events retrieved from the database.
     *
     * @param id              The unique event ID from the database.
     * @param patientName     The name of the patient.
     * @param doctorName      The name of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param status          The status of the appointment (Scheduled, Completed, etc.).
     * @param notes           Any additional notes for the appointment.
     * @param location        The location of the appointment.
     * @param pdfUri          The URI of the attached PDF file (nullable).
     */
    public Event(long id, String patientName, String doctorName, String appointmentDate, 
                 String status, String notes, String location, String pdfUri) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
        this.location = location;
        this.pdfUri = pdfUri;
    }

    /**
     * Constructor for a new event (without ID).
     * Used when the user creates an event before saving it in the database.
     *
     * @param patientName     The name of the patient.
     * @param doctorName      The name of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param status          The status of the appointment.
     * @param notes           Additional notes related to the event.
     * @param location        The location of the appointment.
     * @param pdfUri          The URI of the attached PDF file (nullable).
     */
    public Event(String patientName, String doctorName, String appointmentDate, 
                 String status, String notes, String location, String pdfUri) {
        this(0, patientName, doctorName, appointmentDate, status, notes, location, pdfUri); // Default ID to 0 for new events
    }

    // -------------------- Parcelable Implementation --------------------

    /**
     * Constructor used to recreate an Event object from a Parcel.
     * This is necessary for passing Event objects between Android components.
     *
     * @param in The Parcel containing serialized Event data.
     */
    protected Event(Parcel in) {
        id = in.readLong();
        patientName = in.readString();
        doctorName = in.readString();
        appointmentDate = in.readString();
        status = in.readString();
        notes = in.readString();
        location = in.readString();
        pdfUri = in.readString(); // May be null, handle appropriately in UI
    }

    /**
     * Writes Event object data to a Parcel for serialization.
     * Enables Event objects to be passed between Activities, Fragments, etc.
     *
     * @param dest  The Parcel to write data to.
     * @param flags Additional flags (not used, typically 0).
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(patientName);
        dest.writeString(doctorName);
        dest.writeString(appointmentDate);
        dest.writeString(status);
        dest.writeString(notes);
        dest.writeString(location);
        dest.writeString(pdfUri);
    }

    /**
     * Describes the contents of this Parcelable object (not used, returns 0).
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcelable.Creator implementation for creating Event instances from a Parcel.
     */
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    // -------------------- Getters and Setters --------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(String pdfUri) {
        this.pdfUri = pdfUri;
    }
}