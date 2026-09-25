package com.healthdesk.controller;

import com.healthdesk.database.DoctorDAO;
import com.healthdesk.model.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for Doctor Management module.
 */
public class DoctorController {

    @FXML private TextField searchField;
    @FXML private Label doctorCountLabel;

    @FXML private TableView<Doctor> doctorTableView;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colSpec;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, String> colRoom;
    @FXML private TableColumn<Doctor, String> colAvailable;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> specCombo;
    @FXML private TextField phoneField;
    @FXML private TextField roomField;
    @FXML private TextField availableTimeField;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private Doctor selectedDoctor = null;

    @FXML
    public void initialize() {
        specCombo.setItems(FXCollections.observableArrayList(
                "Cardiology", "Neurology", "Medicine", "Orthopedics", "Dermatology",
                "Pediatrics", "Gynecology & Obstetrics", "General Surgery", "Ophthalmology", "ENT"
        ));
        specCombo.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("availableTime"));

        doctorTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadDoctors();
    }

    private void loadDoctors() {
        doctorList.clear();
        doctorList.addAll(doctorDAO.getAllDoctors());
        doctorTableView.setItems(doctorList);
        doctorCountLabel.setText("Total Doctors: " + doctorList.size());
    }

    private void populateForm(Doctor d) {
        selectedDoctor = d;
        nameField.setText(d.getName());
        specCombo.setValue(d.getSpecialization());
        phoneField.setText(d.getPhone());
        roomField.setText(d.getRoom());
        availableTimeField.setText(d.getAvailableTime());
    }

    @FXML
    private void handleAddDoctor() {
        String name = nameField.getText();
        String spec = specCombo.getValue();
        String phone = phoneField.getText();
        String room = roomField.getText();
        String time = availableTimeField.getText();

        if (!validateInput(name, spec, phone, room, time)) return;

        Doctor doc = new Doctor(name.trim(), spec.trim(), phone.trim(), room.trim(), time.trim());
        boolean ok = doctorDAO.addDoctor(doc);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor " + doc.getName() + " added successfully.");
            handleClearForm();
            loadDoctors();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add doctor.");
        }
    }

    @FXML
    private void handleUpdateDoctor() {
        if (selectedDoctor == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a doctor from the table to update.");
            return;
        }

        String name = nameField.getText();
        String spec = specCombo.getValue();
        String phone = phoneField.getText();
        String room = roomField.getText();
        String time = availableTimeField.getText();

        if (!validateInput(name, spec, phone, room, time)) return;

        selectedDoctor.setName(name.trim());
        selectedDoctor.setSpecialization(spec.trim());
        selectedDoctor.setPhone(phone.trim());
        selectedDoctor.setRoom(room.trim());
        selectedDoctor.setAvailableTime(time.trim());

        boolean ok = doctorDAO.updateDoctor(selectedDoctor);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor record updated successfully.");
            loadDoctors();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update doctor.");
        }
    }

    @FXML
    private void handleDeleteDoctor() {
        if (selectedDoctor == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a doctor from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete doctor " + selectedDoctor.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete Doctor");
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            boolean ok = doctorDAO.deleteDoctor(selectedDoctor.getId());
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Doctor removed from registry.");
                handleClearForm();
                loadDoctors();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete doctor.");
            }
        }
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText();
        List<Doctor> results = doctorDAO.searchDoctors(q);
        doctorList.clear();
        doctorList.addAll(results);
        doctorTableView.setItems(doctorList);
        doctorCountLabel.setText("Found: " + results.size() + " doctors");
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        handleClearForm();
        loadDoctors();
    }

    @FXML
    private void handleClearForm() {
        selectedDoctor = null;
        doctorTableView.getSelectionModel().clearSelection();
        nameField.clear();
        specCombo.getSelectionModel().selectFirst();
        phoneField.clear();
        roomField.clear();
        availableTimeField.clear();
    }

    private boolean validateInput(String name, String spec, String phone, String room, String time) {
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Doctor name is required.");
            return false;
        }
        if (spec == null || spec.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Specialization is required.");
            return false;
        }
        if (phone == null || phone.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Phone number is required.");
            return false;
        }
        if (room == null || room.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Room number is required.");
            return false;
        }
        if (time == null || time.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Available duty time is required.");
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
