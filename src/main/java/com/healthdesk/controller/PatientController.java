package com.healthdesk.controller;

import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for Patient Management module.
 */
public class PatientController {

    @FXML private TextField searchField;
    @FXML private Label patientCountLabel;

    @FXML private TableView<Patient> patientTableView;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, String> colBloodGroup;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colEmergency;

    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField emergencyContactField;

    private final PatientDAO patientDAO = new PatientDAO();
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Patient selectedPatient = null;

    @FXML
    public void initialize() {
        // Setup dropdowns
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderCombo.getSelectionModel().selectFirst();

        bloodGroupCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        bloodGroupCombo.getSelectionModel().selectFirst();

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmergency.setCellValueFactory(new PropertyValueFactory<>("emergencyContact"));

        // Table selection listener
        patientTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadPatients();
    }

    private void loadPatients() {
        patientList.clear();
        patientList.addAll(patientDAO.getAllPatients());
        patientTableView.setItems(patientList);
        patientCountLabel.setText("Total Patients: " + patientList.size());
    }

    private void populateForm(Patient p) {
        selectedPatient = p;
        nameField.setText(p.getName());
        ageField.setText(String.valueOf(p.getAge()));
        genderCombo.getSelectionModel().select(p.getGender());
        bloodGroupCombo.getSelectionModel().select(p.getBloodGroup());
        phoneField.setText(p.getPhone());
        addressField.setText(p.getAddress());
        emergencyContactField.setText(p.getEmergencyContact());
    }

    @FXML
    private void handleAddPatient() {
        String name = nameField.getText();
        String ageStr = ageField.getText();
        String gender = genderCombo.getValue();
        String blood = bloodGroupCombo.getValue();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String emContact = emergencyContactField.getText();

        if (!validateInput(name, ageStr, phone)) return;

        int age = Integer.parseInt(ageStr.trim());
        Patient newPatient = new Patient(name.trim(), age, gender, blood, phone.trim(), address != null ? address.trim() : "", emContact != null ? emContact.trim() : "");
        boolean ok = patientDAO.addPatient(newPatient);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient " + newPatient.getName() + " registered successfully.");
            handleClearForm();
            loadPatients();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add patient to database.");
        }
    }

    @FXML
    private void handleUpdatePatient() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patient from the table to update.");
            return;
        }

        String name = nameField.getText();
        String ageStr = ageField.getText();
        String gender = genderCombo.getValue();
        String blood = bloodGroupCombo.getValue();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String emContact = emergencyContactField.getText();

        if (!validateInput(name, ageStr, phone)) return;

        selectedPatient.setName(name.trim());
        selectedPatient.setAge(Integer.parseInt(ageStr.trim()));
        selectedPatient.setGender(gender);
        selectedPatient.setBloodGroup(blood);
        selectedPatient.setPhone(phone.trim());
        selectedPatient.setAddress(address != null ? address.trim() : "");
        selectedPatient.setEmergencyContact(emContact != null ? emContact.trim() : "");

        boolean ok = patientDAO.updatePatient(selectedPatient);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient updated successfully.");
            loadPatients();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update patient.");
        }
    }

    @FXML
    private void handleDeletePatient() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patient from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete patient " + selectedPatient.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete Confirmation");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean ok = patientDAO.deletePatient(selectedPatient.getId());
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Patient record deleted.");
                handleClearForm();
                loadPatients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete patient.");
            }
        }
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText();
        List<Patient> results = patientDAO.searchPatients(q);
        patientList.clear();
        patientList.addAll(results);
        patientTableView.setItems(patientList);
        patientCountLabel.setText("Found: " + results.size() + " patients");
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        handleClearForm();
        loadPatients();
    }

    @FXML
    private void handleClearForm() {
        selectedPatient = null;
        patientTableView.getSelectionModel().clearSelection();
        nameField.clear();
        ageField.clear();
        phoneField.clear();
        addressField.clear();
        emergencyContactField.clear();
        genderCombo.getSelectionModel().selectFirst();
        bloodGroupCombo.getSelectionModel().selectFirst();
    }

    private boolean validateInput(String name, String ageStr, String phone) {
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Patient full name is required.");
            return false;
        }
        if (ageStr == null || ageStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Patient age is required.");
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age <= 0 || age > 130) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid age between 1 and 130.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Age must be a numeric integer.");
            return false;
        }
        if (phone == null || phone.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Phone number is required.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
