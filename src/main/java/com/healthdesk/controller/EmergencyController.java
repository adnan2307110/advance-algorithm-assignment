package com.healthdesk.controller;

import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.EmergencyPatient;
import com.healthdesk.model.Patient;
import com.healthdesk.service.EmergencyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller for Emergency Queue Management module.
 * Leverages Java PriorityQueue for sorting by triage priority level.
 */
public class EmergencyController {

    @FXML private Label lblUpNext;
    @FXML private TableView<EmergencyPatient> emergencyTableView;
    @FXML private TableColumn<EmergencyPatient, String> colPriority;
    @FXML private TableColumn<EmergencyPatient, String> colPatient;
    @FXML private TableColumn<EmergencyPatient, String> colCondition;
    @FXML private TableColumn<EmergencyPatient, String> colStatus;
    @FXML private CheckBox chkShowAll;

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea conditionArea;

    @FXML private Label lblP1Count;
    @FXML private Label lblP2Count;
    @FXML private Label lblP3Count;

    private final EmergencyService emergencyService = EmergencyService.getInstance();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ObservableList<EmergencyPatient> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Priority options
        priorityCombo.setItems(FXCollections.observableArrayList(
                "1 - Emergency (Immediate / Critical Life Threat)",
                "2 - Critical (Severe injury / acute distress)",
                "3 - Normal (Urgent / stable condition)"
        ));
        priorityCombo.getSelectionModel().selectFirst();

        // Patient combo converter
        patientCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getName() + " (Age: " + p.getAge() + ", Blood: " + p.getBloodGroup() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        // Table column bindings
        colPriority.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPriorityLabel()));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadPatients();
        loadQueueData();
    }

    private void loadPatients() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
    }

    @FXML
    public void loadQueueData() {
        tableData.clear();
        if (chkShowAll.isSelected()) {
            tableData.addAll(emergencyService.getFullHistory());
        } else {
            tableData.addAll(emergencyService.getWaitingSnapshot());
        }
        emergencyTableView.setItems(tableData);

        // Update peek up-next
        EmergencyPatient next = emergencyService.peekNextPatient();
        if (next != null) {
            lblUpNext.setText("Next in Line: " + next.getPatientName() + " (" + next.getPriorityLabel() + ")");
        } else {
            lblUpNext.setText("Next in Line: None (Queue is clear)");
        }

        // Calculate counts
        int p1 = 0, p2 = 0, p3 = 0;
        for (EmergencyPatient ep : emergencyService.getWaitingSnapshot()) {
            if (ep.getPriority() == 1) p1++;
            else if (ep.getPriority() == 2) p2++;
            else if (ep.getPriority() == 3) p3++;
        }
        lblP1Count.setText("Priority 1: " + p1);
        lblP2Count.setText("Priority 2: " + p2);
        lblP3Count.setText("Priority 3: " + p3);
    }

    @FXML
    private void handleProcessNext() {
        EmergencyPatient next = emergencyService.processNextPatient();
        if (next != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Triage: Processing Patient");
            alert.setHeaderText("Now Treating: " + next.getPatientName());
            alert.setContentText("Priority: " + next.getPriorityLabel() +
                    "\nChief Condition: " + next.getCondition() +
                    "\nStatus: Transferred to Emergency Trauma Unit.");
            alert.showAndWait();
            loadQueueData();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Empty Queue", "There are currently no waiting patients in the emergency triage queue.");
        }
    }

    @FXML
    private void handleEnqueue() {
        Patient patient = patientCombo.getValue();
        String priorityStr = priorityCombo.getValue();
        String condition = conditionArea.getText();

        if (patient == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a registered patient.");
            return;
        }
        if (condition == null || condition.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please describe the patient's symptoms or acute condition.");
            return;
        }

        int priority = 3;
        if (priorityStr != null) {
            if (priorityStr.startsWith("1")) priority = 1;
            else if (priorityStr.startsWith("2")) priority = 2;
            else priority = 3;
        }

        boolean ok = emergencyService.enqueuePatient(patient.getId(), patient.getName(), priority, condition.trim());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Admitted to Triage",
                    "Patient " + patient.getName() + " has been added to the PriorityQueue with Priority " + priority + ".");
            handleClear();
            loadQueueData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add emergency patient.");
        }
    }

    @FXML
    private void handleClear() {
        patientCombo.getSelectionModel().clearSelection();
        priorityCombo.getSelectionModel().selectFirst();
        conditionArea.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
