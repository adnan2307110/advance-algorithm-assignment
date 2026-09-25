package com.healthdesk.controller;

import com.healthdesk.database.AppointmentDAO;
import com.healthdesk.database.DoctorDAO;
import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.Appointment;
import com.healthdesk.model.Doctor;
import com.healthdesk.model.Patient;
import com.healthdesk.service.NotificationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Appointment Management.
 * Enforces duplicate appointment prevention for doctors.
 */
public class AppointmentController {

    @FXML private TextField filterField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label apptSummaryLabel;

    @FXML private TableView<Appointment> appointmentTableView;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colStatus;

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeCombo;
    @FXML private ComboBox<String> statusCombo;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final ObservableList<Appointment> masterList = FXCollections.observableArrayList();
    private Appointment selectedAppointment = null;

    @FXML
    public void initialize() {
        // Setup dropdowns
        statusCombo.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Cancelled"));
        statusCombo.getSelectionModel().select("Scheduled");

        statusFilterCombo.setItems(FXCollections.observableArrayList("All Statuses", "Scheduled", "Completed", "Cancelled"));
        statusFilterCombo.getSelectionModel().select("All Statuses");

        timeCombo.setItems(FXCollections.observableArrayList(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        ));
        timeCombo.getSelectionModel().select("10:00");
        datePicker.setValue(LocalDate.now());

        // Setup converters for patient and doctor combos
        setupConverters();

        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDoctorName() +
                (!cell.getValue().getDoctorSpecialization().isEmpty() ? " (" + cell.getValue().getDoctorSpecialization() + ")" : "")
        ));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        appointmentTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadDropdownData();
        loadAppointments();
    }

    private void setupConverters() {
        patientCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getName() + " (ID: " + p.getId() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        doctorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : d.getName() + " - " + d.getSpecialization();
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });
    }

    private void loadDropdownData() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
        doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.getAllDoctors()));
    }

    private void loadAppointments() {
        masterList.clear();
        masterList.addAll(appointmentDAO.getAllAppointments());
        applyFilter();
    }

    private void populateForm(Appointment appt) {
        selectedAppointment = appt;
        // Match patient
        for (Patient p : patientCombo.getItems()) {
            if (p.getId() == appt.getPatientId()) {
                patientCombo.getSelectionModel().select(p);
                break;
            }
        }
        // Match doctor
        for (Doctor d : doctorCombo.getItems()) {
            if (d.getId() == appt.getDoctorId()) {
                doctorCombo.getSelectionModel().select(d);
                break;
            }
        }
        try {
            datePicker.setValue(LocalDate.parse(appt.getDate()));
        } catch (Exception ignored) {}
        timeCombo.setValue(appt.getTime());
        statusCombo.getSelectionModel().select(appt.getStatus());
    }

    @FXML
    private void handleBookAppointment() {
        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String time = timeCombo.getValue();
        String status = statusCombo.getValue();

        if (patient == null || doctor == null || date == null || time == null || time.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a patient, doctor, date, and time slot.");
            return;
        }

        String dateStr = date.toString();
        // Check duplicate appointment
        if (appointmentDAO.isDuplicateAppointment(doctor.getId(), dateStr, time.trim(), 0)) {
            showAlert(Alert.AlertType.ERROR, "Scheduling Conflict",
                    "Doctor " + doctor.getName() + " already has an active appointment at " + time + " on " + dateStr + ".\nPlease select another time slot or date.");
            return;
        }

        Appointment appt = new Appointment(patient.getId(), doctor.getId(), dateStr, time.trim(), status);
        boolean ok = appointmentDAO.addAppointment(appt);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment successfully booked for " + patient.getName() + ".");
            NotificationService.getInstance().publishNotification(
                    "New Appointment booked: " + patient.getName() + " with " + doctor.getName() + " at " + time + " on " + dateStr
            );
            handleClearForm();
            loadAppointments();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to book appointment.");
        }
    }

    @FXML
    private void handleUpdateAppointment() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment from the table to update.");
            return;
        }

        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String time = timeCombo.getValue();
        String status = statusCombo.getValue();

        if (patient == null || doctor == null || date == null || time == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please complete all fields.");
            return;
        }

        String dateStr = date.toString();
        if (appointmentDAO.isDuplicateAppointment(doctor.getId(), dateStr, time.trim(), selectedAppointment.getId())) {
            showAlert(Alert.AlertType.ERROR, "Scheduling Conflict",
                    "Doctor " + doctor.getName() + " already has another appointment at " + time + " on " + dateStr + ".");
            return;
        }

        selectedAppointment.setPatientId(patient.getId());
        selectedAppointment.setDoctorId(doctor.getId());
        selectedAppointment.setDate(dateStr);
        selectedAppointment.setTime(time.trim());
        selectedAppointment.setStatus(status);

        boolean ok = appointmentDAO.updateAppointment(selectedAppointment);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment updated successfully.");
            loadAppointments();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update appointment.");
        }
    }

    @FXML
    private void handleMarkCompleted() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment first.");
            return;
        }
        appointmentDAO.updateAppointmentStatus(selectedAppointment.getId(), "Completed");
        loadAppointments();
    }

    @FXML
    private void handleCancelAppointment() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an appointment first.");
            return;
        }
        appointmentDAO.updateAppointmentStatus(selectedAppointment.getId(), "Cancelled");
        loadAppointments();
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    private void applyFilter() {
        String filter = filterField.getText() != null ? filterField.getText().toLowerCase().trim() : "";
        String statusFilter = statusFilterCombo.getValue();
        if (statusFilter == null) statusFilter = "All Statuses";

        List<Appointment> filtered = new ArrayList<>();
        int scheduledCount = 0;

        for (Appointment a : masterList) {
            boolean matchesText = filter.isEmpty() ||
                    a.getPatientName().toLowerCase().contains(filter) ||
                    a.getDoctorName().toLowerCase().contains(filter) ||
                    a.getDate().contains(filter);

            boolean matchesStatus = statusFilter.equals("All Statuses") || a.getStatus().equalsIgnoreCase(statusFilter);

            if (matchesText && matchesStatus) {
                filtered.add(a);
            }
            if ("Scheduled".equalsIgnoreCase(a.getStatus())) {
                scheduledCount++;
            }
        }

        appointmentTableView.setItems(FXCollections.observableArrayList(filtered));
        apptSummaryLabel.setText("Total: " + masterList.size() + " | Scheduled: " + scheduledCount);
    }

    @FXML
    private void handleReset() {
        filterField.clear();
        statusFilterCombo.getSelectionModel().select("All Statuses");
        handleClearForm();
        loadDropdownData();
        loadAppointments();
    }

    @FXML
    private void handleClearForm() {
        selectedAppointment = null;
        appointmentTableView.getSelectionModel().clearSelection();
        patientCombo.getSelectionModel().clearSelection();
        doctorCombo.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        timeCombo.getSelectionModel().select("10:00");
        statusCombo.getSelectionModel().select("Scheduled");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
