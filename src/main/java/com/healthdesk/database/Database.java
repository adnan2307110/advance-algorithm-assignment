package com.healthdesk.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Manages SQLite database connection and initial schema migration / seeding.
 */
public class Database {
    private static final String DB_DIR = "database";
    private static final String DB_PATH = "database/healthdesk.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private static Connection connection = null;

    static {
        initialize();
    }

    /**
     * Retrieves an active database connection.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return connection;
    }

    /**
     * Initializes database directory, tables, and seed data.
     */
    public static synchronized void initialize() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                // 1. Users table
                stmt.execute("CREATE TABLE IF NOT EXISTS User (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "full_name TEXT NOT NULL" +
                        ");");

                // 2. Patient table
                stmt.execute("CREATE TABLE IF NOT EXISTS Patient (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "age INTEGER NOT NULL, " +
                        "gender TEXT NOT NULL, " +
                        "blood_group TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "address TEXT, " +
                        "emergency_contact TEXT" +
                        ");");

                // 3. Doctor table
                stmt.execute("CREATE TABLE IF NOT EXISTS Doctor (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "specialization TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "room TEXT NOT NULL, " +
                        "available_time TEXT NOT NULL" +
                        ");");

                // 4. Appointment table
                stmt.execute("CREATE TABLE IF NOT EXISTS Appointment (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_id INTEGER NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "time TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE" +
                        ");");

                // 5. Emergency Queue table
                stmt.execute("CREATE TABLE IF NOT EXISTS EmergencyQueue (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_id INTEGER NOT NULL, " +
                        "priority INTEGER NOT NULL, " +
                        "condition TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "arrival_time INTEGER NOT NULL, " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE CASCADE" +
                        ");");

                // 6. Bed table
                stmt.execute("CREATE TABLE IF NOT EXISTS Bed (" +
                        "id TEXT PRIMARY KEY, " +
                        "ward TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "patient_id INTEGER, " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE SET NULL" +
                        ");");

                // 7. LabTest table
                stmt.execute("CREATE TABLE IF NOT EXISTS LabTest (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_id INTEGER NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "test_name TEXT NOT NULL, " +
                        "result TEXT, " +
                        "status TEXT NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE" +
                        ");");

                // 8. Prescription table
                stmt.execute("CREATE TABLE IF NOT EXISTS Prescription (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_id INTEGER NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "medicine TEXT NOT NULL, " +
                        "dosage TEXT NOT NULL, " +
                        "duration TEXT NOT NULL, " +
                        "instructions TEXT, " +
                        "date TEXT NOT NULL, " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE" +
                        ");");

                // 9. Bill table
                stmt.execute("CREATE TABLE IF NOT EXISTS Bill (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "patient_id INTEGER NOT NULL, " +
                        "consultation_fee REAL NOT NULL DEFAULT 0.0, " +
                        "lab_fee REAL NOT NULL DEFAULT 0.0, " +
                        "bed_fee REAL NOT NULL DEFAULT 0.0, " +
                        "medicine_fee REAL NOT NULL DEFAULT 0.0, " +
                        "other_fee REAL NOT NULL DEFAULT 0.0, " +
                        "total REAL NOT NULL DEFAULT 0.0, " +
                        "date TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'Pending', " +
                        "FOREIGN KEY(patient_id) REFERENCES Patient(id) ON DELETE CASCADE" +
                        ");");

                // Seed initial data if tables are freshly created
                seedInitialData(stmt);
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Statement stmt) throws SQLException {
        // Seed Users
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM User;");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO User (username, password, role, full_name) VALUES " +
                    "('admin', 'admin123', 'Admin', 'Dr. Evelyn Vance - System Admin'), " +
                    "('reception', 'rec123', 'Receptionist', 'Sarah Connor - Front Desk'), " +
                    "('doctor', 'doc123', 'Doctor', 'Dr. Adnan Rahman'), " +
                    "('drkarim', 'doc123', 'Doctor', 'Dr. Tariq Karim'), " +
                    "('lab', 'lab123', 'Lab Technician', 'Alex Mercer - Lab Tech');");
        }

        // Seed Doctors
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Doctor;");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Doctor (name, specialization, phone, room, available_time) VALUES " +
                    "('Dr. Adnan Rahman', 'Cardiology', '01711-223344', '101', '09:00 AM - 01:00 PM'), " +
                    "('Dr. Tariq Karim', 'Neurology', '01722-334455', '202', '10:00 AM - 02:00 PM'), " +
                    "('Dr. Sabrina Khan', 'Medicine', '01733-445566', '105', '02:00 PM - 06:00 PM'), " +
                    "('Dr. Farhan Ahmed', 'Orthopedics', '01744-556677', '301', '11:00 AM - 04:00 PM'), " +
                    "('Dr. Nusrat Jahan', 'Dermatology', '01755-667788', '204', '04:00 PM - 08:00 PM');");
        }

        // Seed Patients
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Patient;");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Patient (name, age, gender, blood_group, phone, address, emergency_contact) VALUES " +
                    "('Adnan Hossain', 21, 'Male', 'B+', '01811-998877', 'Dhanmondi, Dhaka', '01711-001122'), " +
                    "('Rahim Uddin', 35, 'Male', 'O+', '01822-887766', 'Agrabad, Chittagong', '01722-112233'), " +
                    "('Fatima Begum', 42, 'Female', 'A+', '01833-776655', 'Zindabazar, Sylhet', '01733-223344'), " +
                    "('Sadia Akter', 28, 'Female', 'AB+', '01844-665544', 'Kazihata, Rajshahi', '01744-334455'), " +
                    "('Tanvir Hasan', 19, 'Male', 'O-', '01855-554433', 'Boyra, Khulna', '01755-445566');");
        }

        // Seed Beds
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Bed;");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO Bed (id, ward, status, patient_id) VALUES " +
                    "('B-101', 'General', 'Occupied', 1), " +
                    "('B-102', 'General', 'Available', NULL), " +
                    "('B-103', 'ICU', 'Occupied', 2), " +
                    "('B-104', 'ICU', 'Available', NULL), " +
                    "('B-105', 'Surgery', 'Available', NULL), " +
                    "('B-106', 'Surgery', 'Occupied', 3), " +
                    "('B-107', 'Maternity', 'Available', NULL), " +
                    "('B-108', 'Pediatric', 'Maintenance', NULL);");
        }

        // Seed Appointments
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Appointment;");
        if (rs.next() && rs.getInt(1) == 0) {
            String today = LocalDate.now().toString();
            String tomorrow = LocalDate.now().plusDays(1).toString();
            stmt.execute("INSERT INTO Appointment (patient_id, doctor_id, date, time, status) VALUES " +
                    "(1, 1, '" + today + "', '10:30', 'Scheduled'), " +
                    "(2, 2, '" + today + "', '11:15', 'Scheduled'), " +
                    "(3, 3, '" + today + "', '14:30', 'Scheduled'), " +
                    "(4, 4, '" + tomorrow + "', '11:30', 'Scheduled'), " +
                    "(5, 1, '" + today + "', '09:30', 'Completed');");
        }

        // Seed Emergency Queue
        rs = stmt.executeQuery("SELECT COUNT(*) FROM EmergencyQueue;");
        if (rs.next() && rs.getInt(1) == 0) {
            long now = System.currentTimeMillis();
            stmt.execute("INSERT INTO EmergencyQueue (patient_id, priority, condition, status, arrival_time) VALUES " +
                    "(2, 1, 'Acute myocardial infarction / Chest pain', 'Waiting', " + (now - 600000) + "), " +
                    "(5, 2, 'Multiple bone fracture with hemorrhage', 'Waiting', " + (now - 300000) + "), " +
                    "(4, 3, 'High fever with dehydration', 'Waiting', " + (now - 100000) + ");");
        }

        // Seed Lab Tests
        rs = stmt.executeQuery("SELECT COUNT(*) FROM LabTest;");
        if (rs.next() && rs.getInt(1) == 0) {
            String today = LocalDate.now().toString();
            stmt.execute("INSERT INTO LabTest (patient_id, doctor_id, test_name, result, status, date) VALUES " +
                    "(1, 1, 'Complete Blood Count (CBC)', 'Hemoglobin: 14.2 g/dL, WBC: 7,200 /uL, Platelets: 250,000 /uL', 'Completed', '" + today + "'), " +
                    "(2, 2, 'Lipid Profile', 'Total Cholesterol: 210 mg/dL, HDL: 45 mg/dL, LDL: 135 mg/dL', 'Completed', '" + today + "'), " +
                    "(3, 3, 'Blood Glucose (Fasting)', NULL, 'Processing', '" + today + "'), " +
                    "(4, 4, 'Chest X-Ray', NULL, 'Requested', '" + today + "');");
        }

        // Seed Prescriptions
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Prescription;");
        if (rs.next() && rs.getInt(1) == 0) {
            String today = LocalDate.now().toString();
            stmt.execute("INSERT INTO Prescription (patient_id, doctor_id, medicine, dosage, duration, instructions, date) VALUES " +
                    "(1, 1, 'Paracetamol', '500 mg', '5 days', 'Take 1 tablet after meals if fever persists', '" + today + "'), " +
                    "(1, 1, 'Omeprazole', '20 mg', '14 days', 'Take 1 capsule 30 minutes before breakfast', '" + today + "'), " +
                    "(2, 2, 'Atorvastatin', '10 mg', '30 days', 'Take 1 tablet daily at bedtime', '" + today + "');");
        }

        // Seed Bills
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Bill;");
        if (rs.next() && rs.getInt(1) == 0) {
            String today = LocalDate.now().toString();
            stmt.execute("INSERT INTO Bill (patient_id, consultation_fee, lab_fee, bed_fee, medicine_fee, other_fee, total, date, status) VALUES " +
                    "(1, 500.0, 300.0, 1000.0, 450.0, 0.0, 2250.0, '" + today + "', 'Paid'), " +
                    "(2, 600.0, 800.0, 3000.0, 550.0, 150.0, 5100.0, '" + today + "', 'Pending'), " +
                    "(3, 400.0, 200.0, 0.0, 150.0, 0.0, 750.0, '" + today + "', 'Paid');");
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  HealthDesk Database Initializer & Verification  ");
        System.out.println("==================================================");
        initialize();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            printCount(stmt, "User");
            printCount(stmt, "Doctor");
            printCount(stmt, "Patient");
            printCount(stmt, "Bed");
            printCount(stmt, "Appointment");
            printCount(stmt, "EmergencyQueue");
            printCount(stmt, "LabTest");
            printCount(stmt, "Prescription");
            printCount(stmt, "Bill");
        } catch (SQLException e) {
            System.err.println("Verification error: " + e.getMessage());
        }
        System.out.println("==================================================");
        System.out.println("  Database verification completed successfully!   ");
        System.out.println("==================================================");
    }

    private static void printCount(Statement stmt, String tableName) {
        try (var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName + ";")) {
            if (rs.next()) {
                System.out.printf("  Table: %-16s | Records: %d\n", tableName, rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.printf("  Table: %-16s | Error: %s\n", tableName, e.getMessage());
        }
    }
}

