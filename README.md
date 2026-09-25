# HealthDesk – Integrated Healthcare Management System

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-23-FF6F00?logo=java&logoColor=white)](https://openjfx.io/)
[![SQLite](https://img.shields.io/badge/SQLite-3-003B57?logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**HealthDesk** is a desktop-based healthcare management system designed and developed using **Java, JavaFX, SQLite, Object-Oriented Programming (OOP), Multithreading, JSON, and REST API integration**.

The system provides a clinical administration platform for managing the core operational workflows of a hospital or clinic, while adhering strictly to university course syllabus guidelines (no Spring Boot, Hibernate, MySQL, React, Firebase, etc.).

---

## Table of Contents

1. [Features & Modules](#1-features--modules)
2. [Object-Oriented Programming (OOP) Architecture](#2-object-oriented-programming-oop-architecture)
3. [Multithreading Implementation](#3-multithreading-implementation)
4. [External API & JSON Integration](#4-external-api--json-integration)
5. [Database Schema (SQLite)](#5-database-schema-sqlite)
6. [User Roles & Demo Credentials](#6-user-roles--demo-credentials)
7. [Project Structure](#7-project-structure)
8. [Installation & How to Run](#8-installation--how-to-run)
9. [Git Branching & Commit Workflow](#9-git-branching--commit-workflow)

---

## 1. Features & Modules

* **🔐 Authentication & Role-Based Access Control (RBAC):**
  * Secure login with credential validation and role assignment.
  * Dynamically filters sidebar actions and operation permissions according to role (Admin, Receptionist, Doctor, Lab Technician).
  * Quick Demo credential buttons for rapid university viva evaluation.

* **📊 Operations Dashboard:**
  * Real-time statistic metric cards (Total Patients, Doctors, Today's Appointments, Available/Occupied Beds, Emergency Triage count, Pending Lab tests).
  * Today's Outpatient Appointments live schedule.
  * Emergency Queue preview with instant one-click triage processing.
  * Operational shortcut buttons.

* **👥 Patient Management:**
  * Complete CRUD (Register, Update, Delete, View details).
  * Instant multi-attribute search (by name, phone, blood group, address).
  * Demographic tracking: Patient ID, Name, Age, Gender, Blood Group, Phone, Address, Emergency Contact.

* **👨‍⚕️ Doctor Management:**
  * Manage clinical specialists (Cardiology, Neurology, Medicine, Orthopedics, Dermatology, Surgery, Pediatrics, etc.).
  * Tracks doctor name, specialization, phone, room number, and duty hours.
  * Add, edit, remove, and search medical staff.

* **📅 Appointment Management:**
  * Outpatient appointment scheduling and rescheduling.
  * **Duplicate Prevention:** Automatically detects and blocks conflicting appointments for the same doctor at the exact same date and time slot.
  * Status management: `Scheduled`, `Completed`, `Cancelled`.

* **🚨 Emergency Triage Queue (Java PriorityQueue):**
  * Prioritizes emergency admissions using Java's `PriorityQueue<EmergencyPatient>` and custom `Comparable`:
    * **Priority 1:** Emergency (Immediate resuscitation / life threat)
    * **Priority 2:** Critical (Severe trauma, acute injury)
    * **Priority 3:** Normal (Urgent but hemodynamically stable)
  * Ties broken chronologically (FIFO) by arrival timestamp.
  * **"Process Next Patient"** action polls the highest priority patient from the queue, changes status to *In Treatment*, and notifies the trauma unit.

* **🛏️ Hospital Bed & Ward Management:**
  * Real-time occupancy tracking across wards (General, ICU, Surgery, Maternity, Pediatric).
  * Bed statuses: `Available`, `Occupied`, `Maintenance`.
  * Assign available bed to an admitted patient (auto-releases previous assignments).
  * Discharge / release bed back to *Available*.
  * Sanitation / maintenance toggle.

* **🔬 Laboratory Management:**
  * **Doctor ordering:** Request diagnostic tests (Complete Blood Count, Lipid Profile, Blood Glucose, Chest X-Ray, Liver Function Test, etc.).
  * **Technician results:** Enter clinical parameters, diagnostic findings, and save official results.
  * **Multithreaded Simulation:** Simulated background analyzer transitions sample through `Requested` → `Processing` → `Completed` with realistic reference metrics.

* **💊 Prescription Management:**
  * Multi-medicine builder: Doctors can prescribe multiple medications in a single prescription session (Medicine, Dosage, Duration, Directions).
  * Printable/formatted clinical prescription slip viewer with official Rx format.

* **💵 Automated Billing System:**
  * Itemized cost calculation: Consultation Fee, Laboratory Fee, Bed Fee, Medicine Fee, Other Charges.
  * **Automated Total Calculation:** Pure Java calculation method (`Bill.calculateTotal()`) with real-time recalculation upon fee adjustment.
  * Generate, mark paid, and print formatted hospital invoice receipts.

* **📋 Unified Patient Medical History (Dossier):**
  * Select any patient to inspect a unified medical history dossier:
    * Patient Demographics & Current Inpatient Bed status
    * Outpatient Appointment timeline
    * Laboratory test results and clinical findings
    * Medication prescriptions and instructions
    * Lifetime financial invoice records

* **🌐 Health Information Reference API (JSON & HTTP):**
  * Fetches clinical reference data asynchronously in a background worker thread.
  * Parses JSON response into `HealthInfo` domain objects.
  * Search and category filtering (Cardiovascular, Endocrinology, Hematology, Vital Signs, etc.).

---

## 2. Object-Oriented Programming (OOP) Architecture

* **Encapsulation:**
  * All domain entity classes (`Patient`, `Doctor`, `Appointment`, `Bed`, `LabTest`, `Prescription`, `Bill`, `HealthInfo`) feature private attributes exposed via validated getters and setters.
* **Inheritance:**
  * Abstract base class `User` encapsulates common account attributes (`id`, `username`, `password`, `role`, `fullName`).
  * Subclasses inherit and specialize functionality:
    * `Admin extends User`
    * `DoctorUser extends User`
    * `Receptionist extends User`
    * `LabTechnician extends User`
* **Polymorphism:**
  * Polymorphic method overrides such as `getRoleDisplayName()` and `isAdmin()`.
  * Polymorphic UI role permission checking.
* **Collections & Interfaces:**
  * `Comparable<EmergencyPatient>` implemented with custom comparator logic for Java's `PriorityQueue`.
  * `List<T>`, `Set<T>`, `CopyOnWriteArrayList<T>` for concurrency-safe event handling.

---

## 3. Multithreading Implementation

HealthDesk incorporates multithreading for practical hospital operations without freezing the JavaFX application thread:

1. **Appointment Notification Service (`NotificationService.java`):**
   * Uses a scheduled daemon background thread via `ScheduledExecutorService`.
   * Periodically checks SQLite for upcoming appointments scheduled today.
   * Dispatches alerts safely to the JavaFX Application Thread using `Platform.runLater()`.
2. **Laboratory Processing Simulation (`LabService.java`):**
   * Employs an `ExecutorService` thread pool.
   * When diagnostics are launched, the worker thread simulates chemical reaction delay (3.5 seconds), updates sample status (`Requested` → `Processing` → `Completed`), computes clinical values, and notifies the UI.
3. **Asynchronous API & JSON Parsing (`HealthAPI.java`):**
   * Executes HTTP GET requests and JSON parsing asynchronously in worker threads using `CompletableFuture.supplyAsync()`.
   * Prevents UI stutter during network delays.

---

## 4. External API & JSON Integration

The application integrates an external health information pipeline:

```text
HTTP Request (java.net.http.HttpClient)
             ↓
HTTP JSON Response
             ↓
JSON Parsing (org.json.JSONObject & org.json.JSONArray)
             ↓
Java HealthInfo Domain Objects
             ↓
JavaFX ObservableList & TableView / Cards
```

Includes offline fallback data ensuring flawless demonstrations during offline evaluation.

---

## 5. Database Schema (SQLite)

Local relational database located at `database/healthdesk.db`. Uses standard JDBC.

```sql
User (id, username, password, role, full_name)
Patient (id, name, age, gender, blood_group, phone, address, emergency_contact)
Doctor (id, name, specialization, phone, room, available_time)
Appointment (id, patient_id, doctor_id, date, time, status)
EmergencyQueue (id, patient_id, priority, condition, status, arrival_time)
Bed (id, ward, status, patient_id)
LabTest (id, patient_id, doctor_id, test_name, result, status, date)
Prescription (id, patient_id, doctor_id, medicine, dosage, duration, instructions, date)
Bill (id, patient_id, consultation_fee, lab_fee, bed_fee, medicine_fee, other_fee, total, date, status)
```

Foreign key constraints (`PRAGMA foreign_keys = ON;`) are enforced on all relational joins.

---

## 6. User Roles & Demo Credentials

Pre-seeded credentials ready for testing:

| Role | Username | Password | Full Name / Description |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Dr. Evelyn Vance (Full access to all modules) |
| **Receptionist** | `reception` | `rec123` | Sarah Connor (Patients, Appointments, Beds, Emergency, Billing) |
| **Doctor** | `doctor` | `doc123` | Dr. Adnan Rahman (Appointments, Prescriptions, Lab Requests) |
| **Doctor (Neurology)** | `drkarim` | `doc123` | Dr. Tariq Karim (Appointments, Prescriptions, Lab Requests) |
| **Lab Technician** | `lab` | `lab123` | Alex Mercer (Lab Orders, Analysis Simulation, Results) |

*(Quick-fill buttons are provided directly on the Login screen for instant 1-click access during demo).*

---

## 7. Project Structure

```text
HealthDesk/
│
├── pom.xml                               # Maven project descriptor
├── mvnw.cmd                              # Maven wrapper executable
├── run.bat                               # One-click launcher script
├── build.bat                             # One-click build script
├── .gitignore                            # Git ignore configuration
│
├── database/
│   └── healthdesk.db                     # SQLite database (auto-created & seeded)
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── healthdesk/
│       │           ├── Main.java                 # JavaFX Application entrypoint
│       │           ├── MainLauncher.java         # Classpath launcher
│       │           │
│       │           ├── model/                    # Domain models & OOP entities
│       │           │   ├── User.java             # Base abstract user
│       │           │   ├── Admin.java
│       │           │   ├── DoctorUser.java
│       │           │   ├── Receptionist.java
│       │           │   ├── LabTechnician.java
│       │           │   ├── Patient.java
│       │           │   ├── Doctor.java
│       │           │   ├── Appointment.java
│       │           │   ├── EmergencyPatient.java # Implements Comparable
│       │           │   ├── Bed.java
│       │           │   ├── LabTest.java
│       │           │   ├── Prescription.java
│       │           │   ├── Bill.java             # Automated calculation
│       │           │   └── HealthInfo.java
│       │           │
│       │           ├── database/                 # JDBC DAOs & Connection
│       │           │   ├── Database.java         # Connection & DB seeder
│       │           │   ├── UserDAO.java
│       │           │   ├── PatientDAO.java
│       │           │   ├── DoctorDAO.java
│       │           │   ├── AppointmentDAO.java
│       │           │   ├── EmergencyQueueDAO.java
│       │           │   ├── BedDAO.java
│       │           │   ├── LabTestDAO.java
│       │           │   ├── PrescriptionDAO.java
│       │           │   └── BillDAO.java
│       │           │
│       │           ├── service/                  # Business logic & Multithreading
│       │           │   ├── AuthService.java      # Session & RBAC
│       │           │   ├── NotificationService.java # Background scheduled thread
│       │           │   ├── EmergencyService.java # PriorityQueue triage
│       │           │   └── LabService.java       # Background sample simulation
│       │           │
│       │           ├── api/
│       │           │   └── HealthAPI.java        # HTTP Client & JSON Parser
│       │           │
│       │           └── controller/               # JavaFX UI Controllers
│       │               ├── LoginController.java
│       │               ├── MainLayoutController.java
│       │               ├── DashboardController.java
│       │               ├── PatientController.java
│       │               ├── DoctorController.java
│       │               ├── AppointmentController.java
│       │               ├── EmergencyController.java
│       │               ├── BedController.java
│       │               ├── LabController.java
│       │               ├── PrescriptionController.java
│       │               ├── BillingController.java
│       │               ├── PatientHistoryController.java
│       │               └── HealthInfoController.java
│       │
│       └── resources/
│           ├── css/
│           │   └── style.css                     # Modern healthcare CSS styling
│           └── fxml/
│               ├── login.fxml
│               ├── main_layout.fxml
│               ├── dashboard.fxml
│               ├── patient.fxml
│               ├── doctor.fxml
│               ├── appointment.fxml
│               ├── emergency.fxml
│               ├── bed.fxml
│               ├── laboratory.fxml
│               ├── prescription.fxml
│               ├── billing.fxml
│               ├── history.fxml
│               └── health_info.fxml
│
└── README.md
```

---

## 8. Installation & How to Run

### Prerequisites
* **Java 21 or higher** installed.

### Option 1: One-Click Launch (Windows)
Double-click:
```cmd
run.bat
```

### Option 2: Run with Maven
In PowerShell or Command Prompt:
```cmd
.\mvnw.cmd javafx:run
```

### Option 3: Run Standalone Packaged JAR
```cmd
java -jar target\healthdesk-1.0-SNAPSHOT.jar
```

### To Recompile or Package:
```cmd
.\mvnw.cmd clean package -DskipTests
```

---

## 9. Git Branching & Commit Workflow

Developed using structured feature branches merged into `main`:

* `main` — Production-ready release branch
* `database-module` — Models, SQLite database setup, JDBC DAOs
* `multithreading-api` — Notification service, Lab processing simulation, HTTP/JSON API
* `javafx-ui` — Modern JavaFX UI, FXML views, CSS styling, controllers, and packaging
