# Appointment Now - Steward

## Overview
**Appointment Now - Steward** is an Android application designed to help users manage their appointments efficiently. It provides features for scheduling, tracking, and managing appointments with a user-friendly interface.

## Features
- **User Authentication**: Secure login and registration with encrypted password storage.
- **Appointment Management**: Add, edit, delete, and view appointments.
- **Event History**: Track completed, missed, and rescheduled appointments.
- **PDF Export**: Generate and export appointment details as PDF files.
- **SMS Notifications**: Option to enable appointment reminders via SMS.
- **Secure Data Storage**: Uses SQLite for storing appointments securely.

## Technologies Used
- **Programming Language**: Java
- **Framework**: Android SDK
- **Database**: SQLite
- **UI Components**: Material Design
- **Security**: SHA-256 password hashing
- **Permissions**:
  - `SEND_SMS`: For sending appointment reminders
  - `READ_EXTERNAL_STORAGE`: To access stored PDFs
  - `WRITE_EXTERNAL_STORAGE`: To save exported PDFs

## Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/1conorsteward/appointment-now-steward.git
   ```
2. Open the project in Android Studio.
3. Ensure the required dependencies are installed.
4. Build and run the application on an emulator or a physical device.

## Usage Guide
1. **Login/Register**: Users can log in or create a new account.
2. **Create an Appointment**: Tap on "Add Event" to schedule an appointment.
3. **Manage Appointments**: View, edit, delete, or search for appointments.
4. **Export to PDF**: Generate a PDF of appointment details.
5. **Enable SMS Notifications**: Grant SMS permissions for reminders.

## Contributing
1. Fork the repository.
2. Create a new branch (`feature-branch`).
3. Make necessary changes and commit (`git commit -m "Add new feature"`).
4. Push to your branch (`git push origin feature-branch`).
5. Create a pull request.

## License
This project is licensed under the MIT License.

## Contact
For questions or support, contact:
- **Author**: Conor Steward
- **Email**: 1conorsteward@gmail.com