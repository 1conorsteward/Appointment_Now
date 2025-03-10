/*
 *     Appointment Now - SQLite Database Helper
 *     Author: Conor Steward
 *     Contact: 1conorsteward@gmail.com
 *     Date Created: 10/8/24
 *     Last Updated: 03/07/25
 *     Version: 2.2
 *
 *     Description:
 *     This class manages SQLite database interactions for the AppointmentNow application.
 *     It includes user authentication with password hashing, event storage, and optimized queries.
 *
 *     Features:
 *     - Secure user authentication with SHA-256 password hashing.
 *     - CRUD operations for users and events.
 *     - Optimized queries using parameterized statements to prevent SQL injection.
 *     - Foreign key constraints to enforce data integrity.
 * 
 *     Dependencies:
 *     - Android SQLite (`SQLiteOpenHelper`, `SQLiteDatabase`)
 * 
 *     Issues:
 *     - No known issues.
 */

package com.example.appointmentnow_steward;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Configuration
    private static final String DATABASE_NAME = "AppointmentNow.db";
    private static final int DATABASE_VERSION = 4; // Increment this when modifying schema
    private static final String TAG = "DatabaseHelper"; // For logging

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EVENTS = "events";

    // Users Table Columns
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    // Events Table Columns
    private static final String COLUMN_EVENT_ID = "event_id";
    private static final String COLUMN_PATIENT_NAME = "patient_name";
    private static final String COLUMN_DOCTOR_NAME = "doctor_name";
    private static final String COLUMN_APPOINTMENT_DATE = "appointment_date";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_PDF_URI = "pdf_uri";

    // SQLite Query: Users Table Creation
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                    + COLUMN_USER_PASSWORD + " TEXT NOT NULL)"; 

    // SQLite Query: Events Table Creation
    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " ("
                    + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_PATIENT_NAME + " TEXT NOT NULL, "
                    + COLUMN_DOCTOR_NAME + " TEXT NOT NULL, "
                    + COLUMN_APPOINTMENT_DATE + " TEXT NOT NULL, "
                    + COLUMN_STATUS + " TEXT NOT NULL, "
                    + COLUMN_NOTES + " TEXT, "
                    + COLUMN_LOCATION + " TEXT, "
                    + COLUMN_PDF_URI + " TEXT, "
                    + COLUMN_USER_ID + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") "
                    + "ON DELETE CASCADE ON UPDATE CASCADE)"; 

    /**
     * Constructor: Initializes database helper.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates tables when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EVENTS);
    }

    /**
     * Upgrades database schema if the version changes.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // -------------------- User Authentication --------------------

    /**
     * Hashes a password using SHA-256.
     *
     * @param password The raw password input.
     * @return The hashed password.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Password hashing error", e);
            return null;
        }
    }

    /**
     * Validates user login by checking the hashed password.
     *
     * @param email    The email entered by the user.
     * @param password The password entered by the user.
     * @return The user ID if credentials are valid, -1 otherwise.
     */
    public long validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        try (Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?",
                new String[]{email, hashedPassword})) {

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating user: ", e);
        }
        return -1;
    }

    /**
     * Adds a new user to the database with a securely hashed password.
     */
    public long addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String hashedPassword = hashPassword(password);

        if (hashedPassword == null) {
            return -1; // Hashing failed
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, hashedPassword);

        return db.insert(TABLE_USERS, null, values);
    }

    // -------------------- Event Operations --------------------

    /**
     * Fetches events based on user ID and status.
     */
    public Cursor getEventsByStatus(long userId, String status, String searchTerm) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENTS +
                " WHERE " + COLUMN_USER_ID + " = ?" +
                " AND " + COLUMN_STATUS + " = ?" +
                (searchTerm.isEmpty() ? "" : " AND " + COLUMN_PATIENT_NAME + " LIKE ?");

        String[] queryParams = searchTerm.isEmpty()
                ? new String[]{String.valueOf(userId), status}
                : new String[]{String.valueOf(userId), status, "%" + searchTerm + "%"};

        return db.rawQuery(query, queryParams);
    }
}