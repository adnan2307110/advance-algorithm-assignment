package com.healthdesk.controller;

import com.healthdesk.database.BedDAO;
import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.Bed;
import com.healthdesk.model.Patient;
import com.healthdesk.service.NotificationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Bed and Ward Management.
 */
public class BedController {

    @FXML private Label lblTotalBeds;
    @FXML private Label lblAvailableBeds;
    @FXML private Label lblOccupiedBeds;
    @FXML private Label lblMaintenanceBeds;

    @FXML private ComboBox<String> wardFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;

    @FXML private TableView<Bed> bedTableView;
    @FXML private TableColumn<Bed, String> colBedId;
    @FXML private TableColumn<Bed, String> colWard;
    @FXML private TableColumn<Bed, String> colStatus;
    @FXML private TableColumn<Bed, String> colPatient;

    @FXML private Label lblSelectedBedId;
    @FXML private Label lblSelectedBedWard;
    @FXML private Label lblSelectedBedStatus;
    @FXML private Label lblSelectedPatient;
    @FXML private ComboBox<Patient> patientCombo;

    private final BedDAO bedDAO = new BedDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ObservableList<Bed> masterList = FXCollections.observableArrayList();
    private Bed selectedBed = null;

    @FXML
    public void initialize() {
        // Setup filter combos
        wardFilterCombo.setItems(FXCollections.observableArrayList(
                "All Wards", "General", "ICU", "Surgery", "Maternity", "Pediatric"
        ));
        wardFilterCombo.getSelectionModel().selectFirst();

        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All Statuses", "Available", "Occupied", "Maintenance"
        ));
        statusFilterCombo.getSelectionModel().selectFirst();

        // Setup patient combo converter
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

        // Setup table columns
        colBedId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colWard.setCellValueFactory(new PropertyValueFactory<>("ward"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPatient.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPatientName()));

        bedTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateSelectedBed(newVal);
            }
        });

        loadPatients();
        loadBeds();
    }

    private void loadPatients() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
    }

    private void loadBeds() {
        masterList.clear();
        masterList.addAll(bedDAO.getAllBeds());
        applyFilter();
        updateSummaryStats();
    }

    private void updateSummaryStats() {
        int total = masterList.size();
        int avail = 0, occ = 0, maint = 0;
        for (Bed b : masterList) {
            if ("Available".equalsIgnoreCase(b.getStatus())) avail++;
            else if ("Occupied".equalsIgnoreCase(b.getStatus())) occ++;
            else if ("Maintenance".equalsIgnoreCase(b.getStatus())) maint++;
        }
        lblTotalBeds.setText(String.valueOf(total));
        lblAvailableBeds.setText(String.valueOf(avail));
        lblOccupiedBeds.setText(String.valueOf(occ));
        lblMaintenanceBeds.setText(String.valueOf(maint));
    }

    private void populateSelectedBed(Bed b) {
        selectedBed = b;
        lblSelectedBedId.setText("Bed: " + b.getId());
        lblSelectedBedWard.setText("Ward: " + b.getWard());
        lblSelectedBedStatus.setText("Status: " + b.getStatus());
        lblSelectedPatient.setText("Patient: " + b.getPatientName());
    }

    @FXML
    private void handleAssignBed() {
        if (selectedBed == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a bed from the table to assign.");
            return;
        }
        if ("Maintenance".equalsIgnoreCase(selectedBed.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Unavailable", "This bed is currently under maintenance.");
            return;
        }

        Patient patient = patientCombo.getValue();
        if (patient == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a patient to assign to this bed.");
            return;
        }

        boolean ok = bedDAO.assignBed(selectedBed.getId(), patient.getId());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bed " + selectedBed.getId() + " assigned to " + patient.getName() + ".");
            NotificationService.getInstance().publishNotification(
                    "Bed " + selectedBed.getId() + " (" + selectedBed.getWard() + ") assigned to " + patient.getName()
            );
            loadBeds();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to assign bed.");
        }
    }

    @FXML
    private void handleReleaseBed() {
        if (selectedBed == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a bed from the table first.");
            return;
        }
        if (!"Occupied".equalsIgnoreCase(selectedBed.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Not Occupied", "This bed is already not occupied.");
            return;
        }

        boolean ok = bedDAO.releaseBed(selectedBed.getId());
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bed " + selectedBed.getId() + " has been released and is now Available.");
            NotificationService.getInstance().publishNotification(
                    "Bed " + selectedBed.getId() + " (" + selectedBed.getWard() + ") released / discharged."
            );
            loadBeds();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to release bed.");
        }
    }

    @FXML
    private void handleToggleMaintenance() {
        if (selectedBed == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a bed first.");
            return;
        }
        String newStatus = "Maintenance".equalsIgnoreCase(selectedBed.getStatus()) ? "Available" : "Maintenance";
        boolean ok = bedDAO.updateStatus(selectedBed.getId(), newStatus);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Bed " + selectedBed.getId() + " status changed to " + newStatus + ".");
            loadBeds();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update bed status.");
        }
    }

    @FXML
    private void handleAddNewBedDialog() {
        Dialog<Bed> dialog = new Dialog<>();
        dialog.setTitle("Add New Hospital Bed");
        dialog.setHeaderText("Register a new bed into hospital inventory");

        ButtonType btnAdd = new ButtonType("Add Bed", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        TextField idField = new TextField();
        idField.setPromptText("e.g. B-109");
        ComboBox<String> wardCombo = new ComboBox<>(FXCollections.observableArrayList(
                "General", "ICU", "Surgery", "Maternity", "Pediatric"
        ));
        wardCombo.getSelectionModel().selectFirst();

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Available", "Maintenance"
        ));
        statusCombo.getSelectionModel().selectFirst();

        grid.add(new Label("Bed ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Ward:"), 0, 1);
        grid.add(wardCombo, 1, 1);
        grid.add(new Label("Initial Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAdd) {
                String id = idField.getText();
                if (id != null && !id.trim().isEmpty()) {
                    return new Bed(id.trim().toUpperCase(), wardCombo.getValue(), statusCombo.getValue());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newBed -> {
            boolean ok = bedDAO.addBed(newBed.getId(), newBed.getWard(), newBed.getStatus());
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Bed Added", "Bed " + newBed.getId() + " added successfully.");
                loadBeds();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Bed ID might already exist in database.");
            }
        });
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    private void applyFilter() {
        String ward = wardFilterCombo.getValue();
        String status = statusFilterCombo.getValue();
        if (ward == null) ward = "All Wards";
        if (status == null) status = "All Statuses";

        List<Bed> filtered = new ArrayList<>();
        for (Bed b : masterList) {
            boolean matchWard = ward.equals("All Wards") || b.getWard().equalsIgnoreCase(ward);
            boolean matchStatus = status.equals("All Statuses") || b.getStatus().equalsIgnoreCase(status);
            if (matchWard && matchStatus) {
                filtered.add(b);
            }
        }
        bedTableView.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleRefresh() {
        wardFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.getSelectionModel().selectFirst();
        selectedBed = null;
        lblSelectedBedId.setText("Bed: None Selected");
        lblSelectedBedWard.setText("Ward: -");
        lblSelectedBedStatus.setText("Status: -");
        lblSelectedPatient.setText("Patient: None");
        patientCombo.getSelectionModel().clearSelection();
        loadPatients();
        loadBeds();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
