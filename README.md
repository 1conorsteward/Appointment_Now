Developer: Conor Steward
Latest Update: 10/8/24
Version: 2.1

Appointment Now
Appointment Now is an Android-based application designed to help users manage appointments effectively. 
This application includes features such as user authentication, appointment scheduling, tracking, exporting events to PDFs, and maintaining appointment history. 
It is tailored to suit healthcare businesses and individuals who need to organize and track multiple appointments.

Features
User Authentication: Secure login functionality that allows users to authenticate before using the app.
Appointment Scheduling: Users can create, edit, delete, and update appointments, with additional features for attaching notes, locations, and PDFs.
Event Tracking: Every event is tracked, with options to view detailed event history.
Export to PDF: Users can generate PDF documents for appointment summaries and share them easily.
Search & Filter: Search and filter options to locate appointments by date, status, or user.
Appointment History: Maintain and access historical records of completed appointments.
SMS Notifications: Users have the ability to opt-in for SMS reminders for upcoming appointments.
Database Management: Uses SQLite for storing user information, appointment details, and history.

Technologies Used
Programming Language: Java, XML
Database: SQLite
Development Environment: Android Studio

Libraries:
AndroidX
PDFDocument for PDF generation
Retrofit for API integration
Gson for JSON serialization and deserialization
SMS Manager for managing SMS notifications

Folder Structure

AppointmentNow/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/appointmentnow/   # All Java files for the application
│   │   │   │   ├── activities/       # UI-related activities like EventDisplayActivity, HistoryActivity
│   │   │   │   ├── adapters/         # Adapters for RecyclerViews and other UI elements
│   │   │   │   ├── database/         # SQLite database helpers
│   │   │   │   ├── models/           # Models for User, Appointment, Event, etc.
│   │   └── res/
│   │       ├── layout/               # XML layout files for activities and fragments
│   │       ├── values/               # String and color resource files
│   │       └── drawable/             # Image and icon resources
│
└── build/                            # Compiled files for deployment
Installation
Prerequisites
Ensure you have the following installed:

Android Studio
Java Development Kit (JDK)
An Android device or emulator configured in Android Studio.
Clone the Repository
git clone https://github.com/1conorsteward/appointment-now.git
Set Up in Android Studio
Open Android Studio.
Select File > Open and choose the cloned repository folder.
Allow Android Studio to synchronize Gradle and dependencies.
Configure Database
The app uses SQLite as its internal database. You don't need any external setup. The database schema will be initialized the first time the application runs.

Running the Application
To run the application, follow these steps:

Open Android Studio.
Plug in your Android device or start an Android Virtual Device (AVD).
Click on the Run button (green play icon) in Android Studio.
Choose your device/emulator.
The app should now install and start on your chosen device.
API Integration
If using Appointment Now with an external server for appointment synchronization, follow these steps:

Set up the server for handling the appointment requests using an API.
Configure the base API URL in RetrofitClient.java.
private static final String BASE_URL = "https://your-api-server.com/api/";
The app will send and receive appointment data to/from the API for synchronization.

Database Structure
The app uses SQLite to manage user and appointment information. The following tables are created:

User Table
Column	Type	Description
id	INTEGER	Auto-incremented User ID
email	TEXT	User email
password	TEXT	User password (hashed)
sms_permission	INTEGER	0 = No, 1 = Yes
Event Table
Column	Type	Description
id	INTEGER	Auto-incremented Event ID
patient_name	TEXT	Patient’s name
doctor_name	TEXT	Doctor’s name
date	TEXT	Appointment date
status	TEXT	Status of the event
notes	TEXT	Notes related to the event
location	TEXT	Location of the appointment
Contribution Guidelines
Fork the repository.
Create a new branch (git checkout -b feature-branch-name).
Make your changes.
Push to the branch (git push origin feature-branch-name).
Submit a pull request.

License
This project is licensed under the MIT License - see the LICENSE file for details.

For any issues or queries, please feel free to contact the project maintainers.

