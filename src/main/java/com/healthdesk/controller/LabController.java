package com.healthdesk.controller;

import com.healthdesk.database.DoctorDAO;
import com.healthdesk.database.LabTestDAO;
import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.Doctor;
import com.healthdesk.model.LabTest;
import com.healthdesk.model.Patient;
import com.healthdesk.service.AuthService;
import com.healthdesk.service.LabService;
import javafx.application.Platform;
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
 * Controller for Laboratory Management and Multithreaded Processing Simulation.
 */
public class LabController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label labStatsLabel;

    @FXML private TableView<LabTest> labTableView;
    @FXML private TableColumn<LabTest, Integer> colId;
    @FXML private TableColumn<LabTest, String> colDate;
    @FXML private TableColumn<LabTest, String> colPatient;
    @FXML private TableColumn<LabTest, String> colDoctor;
    @FXML private TableColumn<LabTest, String> colTestName;
    @FXML private TableColumn<LabTest, String> colStatus;
    @FXML private TableColumn<LabTest, String> colResult;

    // Doctor Request fields
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private ComboBox<String> testNameCombo;

    // Technician Result fields
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblSelectedTestInfo;
    @FXML private Button btnSimulateProcessing;
    @FXML private TextArea resultArea;

    private final LabTestDAO labTestDAO = new LabTestDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final LabService labService = LabService.getInstance();

    private final ObservableList<LabTest> masterList = FXCollections.observableArrayList();
    private LabTest selectedTest = null;

    @FXML
    public void initialize() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All Statuses", "Requested", "Processing", "Completed"
        ));
        statusFilterCombo.getSelectionModel().selectFirst();

        testNameCombo.setItems(FXCollections.observableArrayList(
                "Complete Blood Count (CBC)",
                "Lipid Profile Panel",
                "Fasting Blood Glucose",
                "Chest X-Ray (PA View)",
                "Liver Function Test (LFT)",
                "Kidney Function Test (KFT / Serum Creatinine)",
                "Urine Routine & Microscopic Examination",
                "Thyroid Stimulating Hormone (TSH)",
                "Serum Electrolytes (Na+, K+, Cl-)",
                "Cardiac Troponin-I"
        ));
        testNameCombo.getSelectionModel().selectFirst();

        setupConverters();

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colTestName.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        labTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateSelectedTest(newVal);
            }
        });

        loadDropdowns();
        loadTests();
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
                return d == null ? "" : d.getName() + " (" + d.getSpecialization() + ")";
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });
    }

    private void loadDropdowns() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
        doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.getAllDoctors()));
    }

    @FXML
    public void loadTests() {
        masterList.clear();
        masterList.addAll(labTestDAO.getAllTests());
        applyFilter();
        updateStats();
    }

    private void updateStats() {
        int pending = 0, completed = 0;
        for (LabTest t : masterList) {
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                completed++;
            } else {
                pending++;
            }
        }
        labStatsLabel.setText("Pending Tests: " + pending + " | Completed: " + completed);
    }

    private void populateSelectedTest(LabTest t) {
        selectedTest = t;
        lblSelectedTestInfo.setText("Selected: #" + t.getId() + " - " + t.getTestName() + " (" + t.getStatus() + ")");
        resultArea.setText(t.getResult() != null ? t.getResult() : "");
    }

    @FXML
    private void handleRequestTest() {
        if (!AuthService.getInstance().canRequestLab()) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only Doctors or Administrators can request diagnostic lab tests.");
            return;
        }

        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        String testName = testNameCombo.getValue();

        if (patient == null || doctor == null || testName == null || testName.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a patient, doctor, and test name.");
            return;
        }

        LabTest test = new LabTest(patient.getId(), doctor.getId(), testName.trim(), null, "Requested", LocalDate.now().toString());
        boolean ok = labService.requestTest(test);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Request Submitted",
                    "Laboratory test order for " + test.getTestName() + " has been sent to the lab queue.");
            loadTests();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit lab test request.");
        }
    }

    @FXML
    private void handleRunSimulation() {
        if (selectedTest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a lab test order from the table to run.");
            return;
        }
        if ("Completed".equalsIgnoreCase(selectedTest.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Completed", "This test has already been completed.");
            return;
        }

        progressIndicator.setVisible(true);
        btnSimulateProcessing.setDisable(true);
        btnSimulateProcessing.setText("⚙️ Analyzing Sample in Background...");

        labService.processTestSimulated(
                selectedTest,
                updatedTest -> {
                    // onStatusUpdate
                    labTableView.refresh();
                    lblSelectedTestInfo.setText("Selected: #" + updatedTest.getId() + " - Processing sample...");
                },
                completedTest -> {
                    // onComplete
                    progressIndicator.setVisible(false);
                    btnSimulateProcessing.setDisable(false);
                    btnSimulateProcessing.setText("⚡ Run Multithreaded Analysis");
                    resultArea.setText(completedTest.getResult());
                    lblSelectedTestInfo.setText("Selected: #" + completedTest.getId() + " - " + completedTest.getTestName() + " (Completed)");
                    loadTests();
                    showAlert(Alert.AlertType.INFORMATION, "Diagnostic Analysis Complete",
                            "Multithreaded laboratory analysis finished successfully for Test #" + completedTest.getId() +
                            ": " + completedTest.getTestName() + ".\n\nResults generated and saved to database.");
                }
        );
    }

    @FXML
    private void handleSaveManualResult() {
        if (selectedTest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a lab test from the table.");
            return;
        }
        String results = resultArea.getText();
        if (results == null || results.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter diagnostic results before saving.");
            return;
        }

        boolean ok = labService.enterManualResult(selectedTest.getId(), results.trim());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Results Saved", "Lab test #" + selectedTest.getId() + " marked as Completed.");
            loadTests();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update lab test result.");
        }
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    private void applyFilter() {
        String filter = statusFilterCombo.getValue();
        if (filter == null || filter.equals("All Statuses")) {
            labTableView.setItems(masterList);
            return;
        }
        List<LabTest> filtered = new ArrayList<>();
        for (LabTest t : masterList) {
            if (t.getStatus().equalsIgnoreCase(filter)) {
                filtered.add(t);
            }
        }
        labTableView.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
