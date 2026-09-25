package com.healthdesk.controller;

import com.healthdesk.database.DoctorDAO;
import com.healthdesk.database.PatientDAO;
import com.healthdesk.database.PrescriptionDAO;
import com.healthdesk.model.Doctor;
import com.healthdesk.model.Patient;
import com.healthdesk.model.Prescription;
import com.healthdesk.service.AuthService;
import com.healthdesk.service.NotificationService;
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
 * Controller for Prescription and Pharmacy module.
 * Supports multi-medicine prescription building.
 */
public class PrescriptionController {

    @FXML private TextField filterField;

    @FXML private TableView<Prescription> prescriptionTableView;
    @FXML private TableColumn<Prescription, Integer> colId;
    @FXML private TableColumn<Prescription, String> colDate;
    @FXML private TableColumn<Prescription, String> colPatient;
    @FXML private TableColumn<Prescription, String> colDoctor;
    @FXML private TableColumn<Prescription, String> colMedicine;
    @FXML private TableColumn<Prescription, String> colDosage;
    @FXML private TableColumn<Prescription, String> colDuration;
    @FXML private TableColumn<Prescription, String> colInstructions;

    // Builder fields
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private TextField medicineField;
    @FXML private TextField dosageField;
    @FXML private TextField durationField;
    @FXML private TextField instructionsField;
    @FXML private ListView<String> currentMedicineListView;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final ObservableList<Prescription> masterList = FXCollections.observableArrayList();
    private final List<PrescriptionItemData> pendingItems = new ArrayList<>();

    public static class PrescriptionItemData {
        public String medicine;
        public String dosage;
        public String duration;
        public String instructions;

        public PrescriptionItemData(String medicine, String dosage, String duration, String instructions) {
            this.medicine = medicine;
            this.dosage = dosage;
            this.duration = duration;
            this.instructions = instructions;
        }

        @Override
        public String toString() {
            return medicine + " (" + dosage + ", " + duration + ") - " + instructions;
        }
    }

    @FXML
    public void initialize() {
        setupConverters();

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colMedicine.setCellValueFactory(new PropertyValueFactory<>("medicine"));
        colDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colInstructions.setCellValueFactory(new PropertyValueFactory<>("instructions"));

        loadDropdowns();
        loadPrescriptions();
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
    public void loadPrescriptions() {
        masterList.clear();
        masterList.addAll(prescriptionDAO.getAllPrescriptions());
        prescriptionTableView.setItems(masterList);
    }

    @FXML
    private void handleAddMedicineItem() {
        String med = medicineField.getText();
        String dos = dosageField.getText();
        String dur = durationField.getText();
        String inst = instructionsField.getText();

        if (med == null || med.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Medicine name is required.");
            return;
        }
        if (dos == null || dos.trim().isEmpty()) {
            dos = "Standard Dose";
        }
        if (dur == null || dur.trim().isEmpty()) {
            dur = "As needed";
        }
        if (inst == null || inst.trim().isEmpty()) {
            inst = "Take as prescribed";
        }

        PrescriptionItemData item = new PrescriptionItemData(med.trim(), dos.trim(), dur.trim(), inst.trim());
        pendingItems.add(item);
        currentMedicineListView.getItems().add(item.toString());

        // Clear sub-inputs
        medicineField.clear();
        dosageField.clear();
        durationField.clear();
        instructionsField.clear();
    }

    @FXML
    private void handleSavePrescription() {
        if (!AuthService.getInstance().canPrescribe()) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only attending Physicians or Administrators can write prescriptions.");
            return;
        }

        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();

        if (patient == null || doctor == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select both a patient and a doctor.");
            return;
        }

        if (pendingItems.isEmpty()) {
            // Check if there is an item in the input fields ready to be added
            String med = medicineField.getText();
            if (med != null && !med.trim().isEmpty()) {
                handleAddMedicineItem();
            } else {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please add at least one medicine to the prescription.");
                return;
            }
        }

        String today = LocalDate.now().toString();
        int savedCount = 0;

        for (PrescriptionItemData item : pendingItems) {
            Prescription p = new Prescription(patient.getId(), doctor.getId(), item.medicine, item.dosage, item.duration, item.instructions, today);
            if (prescriptionDAO.addPrescription(p)) {
                savedCount++;
            }
        }

        showAlert(Alert.AlertType.INFORMATION, "Prescription Saved",
                "Saved " + savedCount + " prescription items for patient " + patient.getName() + " by " + doctor.getName() + ".");

        NotificationService.getInstance().publishNotification(
                "Prescription issued for " + patient.getName() + " by " + doctor.getName()
        );

        handleClearBuilder();
        loadPrescriptions();
    }

    @FXML
    private void handleClearBuilder() {
        pendingItems.clear();
        currentMedicineListView.getItems().clear();
        medicineField.clear();
        dosageField.clear();
        durationField.clear();
        instructionsField.clear();
        patientCombo.getSelectionModel().clearSelection();
        doctorCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleFilter() {
        String filter = filterField.getText() != null ? filterField.getText().toLowerCase().trim() : "";
        if (filter.isEmpty()) {
            prescriptionTableView.setItems(masterList);
            return;
        }
        List<Prescription> filtered = new ArrayList<>();
        for (Prescription p : masterList) {
            if (p.getPatientName().toLowerCase().contains(filter) ||
                p.getMedicine().toLowerCase().contains(filter) ||
                p.getDoctorName().toLowerCase().contains(filter)) {
                filtered.add(p);
            }
        }
        prescriptionTableView.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleViewPrescriptionDialog() {
        Prescription sel = prescriptionTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a prescription from the table to view.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Medical Prescription Slip");
        dialog.setHeaderText("HEALTHDESK CLINICAL PRESCRIPTION");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        String receipt = "=================================================\n" +
                "               HEALTHDESK CLINIC\n" +
                "=================================================\n" +
                "Prescription Ref: #" + sel.getId() + "\n" +
                "Date: " + sel.getDate() + "\n\n" +
                "PATIENT: " + sel.getPatientName() + "\n" +
                "DOCTOR:  " + sel.getDoctorName() + "\n" +
                "-------------------------------------------------\n" +
                "Rx (MEDICATION ORDER):\n\n" +
                "  Medicine:      " + sel.getMedicine() + "\n" +
                "  Dosage:        " + sel.getDosage() + "\n" +
                "  Duration:      " + sel.getDuration() + "\n" +
                "  Instructions:  " + sel.getInstructions() + "\n" +
                "-------------------------------------------------\n" +
                "Doctor Signature: ______________________\n" +
                "=================================================\n";

        TextArea area = new TextArea(receipt);
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        area.setPrefSize(450, 300);

        pane.setContent(area);
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
