package com.healthdesk.controller;

import com.healthdesk.database.*;
import com.healthdesk.model.*;
import com.healthdesk.service.EmergencyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for the Hospital Operations Dashboard.
 */
public class DashboardController {

    @FXML private Label lblTotalPatients;
    @FXML private Label lblTotalDoctors;
    @FXML private Label lblTodayAppointments;
    @FXML private Label lblAvailableBeds;
    @FXML private Label lblTotalBeds;
    @FXML private Label lblOccupiedBeds;
    @FXML private Label lblEmergencyPatients;
    @FXML private Label lblPendingLabTests;

    // Today's appointments table
    @FXML private TableView<Appointment> tblTodayAppointments;
    @FXML private TableColumn<Appointment, String> colApptTime;
    @FXML private TableColumn<Appointment, String> colApptPatient;
    @FXML private TableColumn<Appointment, String> colApptDoctor;
    @FXML private TableColumn<Appointment, String> colApptStatus;

    // Emergency queue table
    @FXML private TableView<EmergencyPatient> tblEmergencyQueue;
    @FXML private TableColumn<EmergencyPatient, String> colEmergPriority;
    @FXML private TableColumn<EmergencyPatient, String> colEmergPatient;
    @FXML private TableColumn<EmergencyPatient, String> colEmergCondition;
    @FXML private TableColumn<EmergencyPatient, String> colEmergStatus;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final LabTestDAO labTestDAO = new LabTestDAO();
    private final EmergencyService emergencyService = EmergencyService.getInstance();

    @FXML
    public void initialize() {
        setupAppointmentTable();
        setupEmergencyTable();
        loadDashboardData();
    }

    private void setupAppointmentTable() {
        colApptTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colApptPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colApptDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colApptStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupEmergencyTable() {
        colEmergPriority.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPriorityLabel()));
        colEmergPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colEmergCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colEmergStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void loadDashboardData() {
        // Statistics
        lblTotalPatients.setText(String.valueOf(patientDAO.getPatientCount()));
        lblTotalDoctors.setText(String.valueOf(doctorDAO.getDoctorCount()));
        lblTodayAppointments.setText(String.valueOf(appointmentDAO.getTodayCount()));

        int totalBeds = bedDAO.getTotalCount();
        int availBeds = bedDAO.getAvailableCount();
        int occBeds = bedDAO.getOccupiedCount();
        lblAvailableBeds.setText(String.valueOf(availBeds));
        lblTotalBeds.setText("/ " + totalBeds + " Total");
        lblOccupiedBeds.setText(occBeds + " Occupied");

        lblEmergencyPatients.setText(String.valueOf(emergencyService.getWaitingCount()));
        lblPendingLabTests.setText(String.valueOf(labTestDAO.getPendingCount()));

        // Tables
        List<Appointment> todayList = appointmentDAO.getTodayAppointments();
        tblTodayAppointments.setItems(FXCollections.observableArrayList(todayList));

        List<EmergencyPatient> emergList = emergencyService.getWaitingSnapshot();
        tblEmergencyQueue.setItems(FXCollections.observableArrayList(emergList));
    }

    @FXML
    private void handleProcessEmergency() {
        EmergencyPatient next = emergencyService.processNextPatient();
        if (next != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Emergency Triage Processed");
            alert.setHeaderText("Processing Patient: " + next.getPatientName());
            alert.setContentText("Priority: " + next.getPriorityLabel() + "\nCondition: " + next.getCondition() +
                    "\nStatus updated to: In Treatment.");
            alert.showAndWait();
            loadDashboardData();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Emergency Queue");
            alert.setHeaderText(null);
            alert.setContentText("No patients currently waiting in the emergency queue.");
            alert.showAndWait();
        }
    }

    // Navigation shortcuts
    @FXML private void navPatients() { MainLayoutController.getInstance().showPatients(); }
    @FXML private void navAppointments() { MainLayoutController.getInstance().showAppointments(); }
    @FXML private void navEmergency() { MainLayoutController.getInstance().showEmergency(); }
    @FXML private void navBeds() { MainLayoutController.getInstance().showBeds(); }
    @FXML private void navLab() { MainLayoutController.getInstance().showLaboratory(); }
    @FXML private void navPrescriptions() { MainLayoutController.getInstance().showPrescriptions(); }
    @FXML private void navBilling() { MainLayoutController.getInstance().showBilling(); }
    @FXML private void navHealthInfo() { MainLayoutController.getInstance().showHealthInfo(); }
}
