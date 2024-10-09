/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    DatabaseHelper.java

    This class, `DatabaseHelper`, is responsible for managing and interacting with the SQLite database
    in the AppointmentNow application. It extends `SQLiteOpenHelper` to perform database-related tasks
    such as creating tables, inserting data, querying data, updating data, and deleting data. It also
    handles database upgrades and ensures foreign key constraints are maintained.

    Key Features:
    - Manages two tables: users and events.
    - Stores user information such as email, password, and SMS permission.
    - Stores event details like patient name, doctor name, appointment date, status, notes, and location.
    - Provides CRUD (Create, Read, Update, Delete) operations for both users and events.
    - Implements foreign key relationships between users and events, ensuring cascading actions.

    Issues: There is a bug that replicates the event that has been most recently added/edited upon logout.
*/

package com.example.appointmentnow_steward;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "AppointmentNow.db";
    private static final int DATABASE_VERSION = 4; // Incremented for updates

    // Table names
    private static final String TABLE_USERS = "users"; // Table for storing user details
    private static final String TABLE_EVENTS = "events"; // Table for storing event details

    // Column names for the users table
    private static final String COLUMN_USER_ID = "user_id"; // Primary key, auto-increment
    private static final String COLUMN_USER_EMAIL = "email"; // User email
    private static final String COLUMN_USER_PASSWORD = "password"; // User password
    private static final String COLUMN_USER_SMS_PERMISSION = "sms_permission"; // User's SMS permission (int flag)

    // Column names for the events table
    private static final String COLUMN_EVENT_ID = "event_id"; // Primary key for events
    private static final String COLUMN_PATIENT_NAME = "patient_name"; // Name of the patient
    private static final String COLUMN_DOCTOR_NAME = "doctor_name"; // Name of the doctor
    private static final String COLUMN_APPOINTMENT_DATE = "appointment_date"; // Appointment date
    private static final String COLUMN_STATUS = "status"; // Status of the event (e.g., Completed, Pending)
    private static final String COLUMN_NOTES = "notes"; // Additional notes for the event
    private static final String COLUMN_LOCATION = "location"; // Location of the appointment
    private static final String COLUMN_PDF_URI = "pdf_uri"; // PDF URI for attached documents

    // SQL statement to create the users table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_EMAIL + " TEXT, "
            + COLUMN_USER_PASSWORD + " TEXT, "
            + COLUMN_USER_SMS_PERMISSION + " INTEGER)"; // Integer flag for SMS permission

    // SQL statement to create the events table
    private static final String CREATE_TABLE_EVENTS = "CREATE TABLE " + TABLE_EVENTS + " ("
            + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "  // Event ID auto-increment
            + COLUMN_PATIENT_NAME + " TEXT, "
            + COLUMN_DOCTOR_NAME + " TEXT, "
            + COLUMN_APPOINTMENT_DATE + " TEXT, "
            + COLUMN_STATUS + " TEXT, "
            + COLUMN_NOTES + " TEXT, "
            + COLUMN_LOCATION + " TEXT, "
            + COLUMN_PDF_URI + " TEXT, "
            + COLUMN_USER_ID + " INTEGER, "  // Foreign key from users table
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE"  // Optional: cascade on delete and update
            + ");";

    // SQL statement to create a view for completed events
    private static final String CREATE_COMPLETED_EVENTS_VIEW = "CREATE VIEW completed_events_view AS " +
            "SELECT " + COLUMN_EVENT_ID + ", " + COLUMN_PATIENT_NAME + ", " + COLUMN_DOCTOR_NAME + ", " +
            COLUMN_APPOINTMENT_DATE + ", " + COLUMN_STATUS + ", " + COLUMN_NOTES + ", " + COLUMN_LOCATION +
            " FROM " + TABLE_EVENTS + " WHERE " + COLUMN_STATUS + " = 'Completed';";

    // SQL statement to create a trigger to ensure user exists in the users table before inserting an event
    private static final String CREATE_TRIGGER_ENSURE_USER_EXISTS = "CREATE TRIGGER ensure_user_exists " +
            "BEFORE INSERT ON " + TABLE_EVENTS + " FOR EACH ROW " +
            "BEGIN " +
            "SELECT CASE WHEN ((SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + " = NEW." + COLUMN_USER_ID + ") IS NULL) " +
            "THEN RAISE (ABORT, 'User does not exist') " +
            "END; " +
            "END;";

    // SQL statement to create an index on the patient name in the events table
    private static final String CREATE_INDEX_PATIENT_NAME = "CREATE INDEX idx_patient_name ON " + TABLE_EVENTS + "(" + COLUMN_PATIENT_NAME + ");";

    // Constructor for the DatabaseHelper
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); // Call superclass with database info
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;"); // Enable foreign keys if writable database
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS); // Create users table
        db.execSQL(CREATE_TABLE_EVENTS); // Create events table
        db.execSQL(CREATE_COMPLETED_EVENTS_VIEW); // Create view for completed events
        db.execSQL(CREATE_TRIGGER_ENSURE_USER_EXISTS); // Create trigger for ensuring user exists
        db.execSQL(CREATE_INDEX_PATIENT_NAME); // Create index on patient name
    }

    // Handle database upgrades
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recreate tables if necessary
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP VIEW IF EXISTS completed_events_view"); // Drop the view if exists
        db.execSQL("DROP TRIGGER IF EXISTS ensure_user_exists"); // Drop the trigger if exists
        db.execSQL("DROP INDEX IF EXISTS idx_patient_name"); // Drop index if exists

        // Recreate the tables with the updated schema
        onCreate(db);
    }

    // Method to retrieve events for a specific user by their user ID
    public Cursor getEventsByUserId(long userId) {
        SQLiteDatabase db = this.getReadableDatabase(); // Get readable database
        Cursor cursor = db.query(TABLE_EVENTS, null, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);
        return cursor;
    }

    // Method to retrieve "completed" events with an optional search term
    public Cursor getCompletedEvents(long userId, String searchTerm) {
        SQLiteDatabase db = this.getReadableDatabase(); // Get readable database
        String selection = COLUMN_USER_ID + " = ? AND " + COLUMN_STATUS + " = 'Completed' AND "
                + COLUMN_PATIENT_NAME + " LIKE ?";
        return db.query(TABLE_EVENTS, null, selection,
                new String[]{String.valueOf(userId), "%" + searchTerm + "%"}, null, null, null);
    }

    // Method to add a new user to the users table
    public long addUser(String email, String password, int smsPermission) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get writable database
        ContentValues values = new ContentValues(); // Create ContentValues to insert data
        values.put(COLUMN_USER_EMAIL, email); // Add email
        values.put(COLUMN_USER_PASSWORD, password); // Add password
        values.put(COLUMN_USER_SMS_PERMISSION, smsPermission); // Add SMS permission
        long id = db.insert(TABLE_USERS, null, values); // Insert into the users table
        db.close(); // Close the database connection
        return id;
    }

    // Method to add a new event to the events table
    public long addEvent(String patientName, String doctorName, String appointmentDate,
                         String status, String notes, String location, String pdfUri, long userId) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get writable database
        ContentValues values = new ContentValues(); // Create ContentValues to insert data
        values.put(COLUMN_PATIENT_NAME, patientName); // Add patient name
        values.put(COLUMN_DOCTOR_NAME, doctorName); // Add doctor name
        values.put(COLUMN_APPOINTMENT_DATE, appointmentDate); // Add appointment date
        values.put(COLUMN_STATUS, status); // Add status
        values.put(COLUMN_NOTES, notes); // Add notes
        values.put(COLUMN_LOCATION, location); // Add location
        values.put(COLUMN_PDF_URI, pdfUri != null ? pdfUri : ""); // Update PDF's, default empty if null
        values.put(COLUMN_USER_ID, userId); // Add user ID (foreign key)

        long id = db.insert(TABLE_EVENTS, null, values); // Insert into the events table
        db.close(); // Close the database connection
        return id; // Return the inserted event ID
    }

    // Method to retrieve a specific event by its ID
    public Cursor getEventById(long id) {
        SQLiteDatabase db = this.getReadableDatabase(); // Get readable database
        return db.query(TABLE_EVENTS, null, COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    // Method to update an event's information in the database
    public boolean updateEvent(long id, String patientName, String doctorName,
                               String appointmentDate, String status, String notes, String location, String pdfUri) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get writable database
        ContentValues values = new ContentValues(); // Create ContentValues to update data
        values.put(COLUMN_PATIENT_NAME, patientName); // Update patient name
        values.put(COLUMN_DOCTOR_NAME, doctorName); // Update doctor name
        values.put(COLUMN_APPOINTMENT_DATE, appointmentDate); // Update appointment date
        values.put(COLUMN_STATUS, status); // Update status
        values.put(COLUMN_NOTES, notes); // Update notes
        values.put(COLUMN_LOCATION, location); // Update location
        values.put(COLUMN_PDF_URI, pdfUri != null ? pdfUri : ""); // Update PDF URI, default empty if null

        int rows = db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(id)}); // Update the event in the database
        db.close(); // Close the database connection
        return rows > 0; // Return true if one or more rows were updated
    }

    // Method to delete an event from the database by its ID
    public boolean deleteEvent(long id) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get writable database
        int rows = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(id)}); // Delete the event by ID
        db.close(); // Close the database connection
        return rows > 0; // Return true if one or more rows were deleted
    }

    // Getter for the users table name
    public String getTableUsers() {
        return TABLE_USERS;
    }

    // Getter for the email column in the users table
    public String getColumnUserEmail() {
        return COLUMN_USER_EMAIL;
    }

    // Getter for the password column in the users table
    public String getColumnUserPassword() {
        return COLUMN_USER_PASSWORD;
    }

    // Getter for the user ID column in the users table
    public String getColumnUserId() {
        return COLUMN_USER_ID;
    }

}
