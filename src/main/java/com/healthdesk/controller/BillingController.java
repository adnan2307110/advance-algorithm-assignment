package com.healthdesk.controller;

import com.healthdesk.database.BillDAO;
import com.healthdesk.database.PatientDAO;
import com.healthdesk.model.Bill;
import com.healthdesk.model.Patient;
import com.healthdesk.service.AuthService;
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
 * Controller for Billing System.
 * Automatically calculates total charges using Java methods.
 */
public class BillingController {

    @FXML private TextField filterField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label revenueLabel;

    @FXML private TableView<Bill> billTableView;
    @FXML private TableColumn<Bill, Integer> colId;
    @FXML private TableColumn<Bill, String> colDate;
    @FXML private TableColumn<Bill, String> colPatient;
    @FXML private TableColumn<Bill, String> colConsultation;
    @FXML private TableColumn<Bill, String> colLab;
    @FXML private TableColumn<Bill, String> colBed;
    @FXML private TableColumn<Bill, String> colMedicine;
    @FXML private TableColumn<Bill, String> colOther;
    @FXML private TableColumn<Bill, String> colTotal;
    @FXML private TableColumn<Bill, String> colStatus;

    // Form fields
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField consultationFeeField;
    @FXML private TextField labFeeField;
    @FXML private TextField bedFeeField;
    @FXML private TextField medicineFeeField;
    @FXML private TextField otherFeeField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label lblCalculatedTotal;

    private final BillDAO billDAO = new BillDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private final ObservableList<Bill> masterList = FXCollections.observableArrayList();
    private Bill currentBillModel = new Bill();

    @FXML
    public void initialize() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("All Statuses", "Paid", "Pending"));
        statusFilterCombo.getSelectionModel().selectFirst();

        statusCombo.setItems(FXCollections.observableArrayList("Pending", "Paid"));
        statusCombo.getSelectionModel().selectFirst();

        setupPatientConverter();

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colConsultation.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getConsultationFee())));
        colLab.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getLabFee())));
        colBed.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getBedFee())));
        colMedicine.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getMedicineFee())));
        colOther.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getOtherFee())));
        colTotal.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getTotal())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup live calculation listeners on fee text fields
        setupLiveCalculations();

        loadPatients();
        loadBills();
        recalculateTotal();
    }

    private void setupPatientConverter() {
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
    }

    private void setupLiveCalculations() {
        consultationFeeField.textProperty().addListener((obs, oldV, newV) -> recalculateTotal());
        labFeeField.textProperty().addListener((obs, oldV, newV) -> recalculateTotal());
        bedFeeField.textProperty().addListener((obs, oldV, newV) -> recalculateTotal());
        medicineFeeField.textProperty().addListener((obs, oldV, newV) -> recalculateTotal());
        otherFeeField.textProperty().addListener((obs, oldV, newV) -> recalculateTotal());
    }

    /**
     * Automatically calculates the total charges using the Java Bill model method.
     */
    private void recalculateTotal() {
        double consult = parseFee(consultationFeeField.getText());
        double lab = parseFee(labFeeField.getText());
        double bed = parseFee(bedFeeField.getText());
        double med = parseFee(medicineFeeField.getText());
        double other = parseFee(otherFeeField.getText());

        currentBillModel.setConsultationFee(consult);
        currentBillModel.setLabFee(lab);
        currentBillModel.setBedFee(bed);
        currentBillModel.setMedicineFee(med);
        currentBillModel.setOtherFee(other);

        double total = currentBillModel.calculateTotal();
        lblCalculatedTotal.setText(String.format("$%.2f", total));
    }

    private double parseFee(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void loadPatients() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
    }

    @FXML
    public void loadBills() {
        masterList.clear();
        masterList.addAll(billDAO.getAllBills());
        applyFilter();
        updateRevenueStats();
    }

    private void updateRevenueStats() {
        double totalPaid = 0.0;
        double totalPending = 0.0;
        for (Bill b : masterList) {
            if ("Paid".equalsIgnoreCase(b.getStatus())) {
                totalPaid += b.getTotal();
            } else {
                totalPending += b.getTotal();
            }
        }
        revenueLabel.setText(String.format("Collected: $%.2f | Outstanding: $%.2f", totalPaid, totalPending));
    }

    @FXML
    private void handleGenerateBill() {
        if (!AuthService.getInstance().canManageBilling()) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Billing is restricted to Receptionists and Administrators.");
            return;
        }

        Patient patient = patientCombo.getValue();
        if (patient == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a patient.");
            return;
        }

        double consult = parseFee(consultationFeeField.getText());
        double lab = parseFee(labFeeField.getText());
        double bed = parseFee(bedFeeField.getText());
        double med = parseFee(medicineFeeField.getText());
        double other = parseFee(otherFeeField.getText());
        String status = statusCombo.getValue();
        String date = LocalDate.now().toString();

        Bill newBill = new Bill(patient.getId(), consult, lab, bed, med, other, date);
        newBill.setStatus(status);

        boolean ok = billDAO.addBill(newBill);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Bill Generated",
                    String.format("Invoice #%d created for %s.\nTotal charges: $%.2f (%s)",
                            newBill.getId(), patient.getName(), newBill.getTotal(), newBill.getStatus()));

            NotificationService.getInstance().publishNotification(
                    String.format("New bill generated for %s: $%.2f (%s)", patient.getName(), newBill.getTotal(), newBill.getStatus())
            );

            loadBills();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save bill to database.");
        }
    }

    @FXML
    private void handleMarkPaid() {
        Bill sel = billTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an invoice from the table.");
            return;
        }
        billDAO.updateStatus(sel.getId(), "Paid");
        loadBills();
        showAlert(Alert.AlertType.INFORMATION, "Payment Confirmed", "Invoice #" + sel.getId() + " marked as Paid in full.");
    }

    @FXML
    private void handleDeleteBill() {
        Bill sel = billTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an invoice from the table.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete invoice #" + sel.getId() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            billDAO.deleteBill(sel.getId());
            loadBills();
        }
    }

    @FXML
    private void handleClearForm() {
        patientCombo.getSelectionModel().clearSelection();
        consultationFeeField.setText("500.0");
        labFeeField.setText("0.0");
        bedFeeField.setText("0.0");
        medicineFeeField.setText("0.0");
        otherFeeField.setText("0.0");
        statusCombo.getSelectionModel().select("Pending");
        recalculateTotal();
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    private void applyFilter() {
        String filter = filterField.getText() != null ? filterField.getText().toLowerCase().trim() : "";
        String statusFilter = statusFilterCombo.getValue();
        if (statusFilter == null) statusFilter = "All Statuses";

        List<Bill> filtered = new ArrayList<>();
        for (Bill b : masterList) {
            boolean matchText = filter.isEmpty() || b.getPatientName().toLowerCase().contains(filter);
            boolean matchStatus = statusFilter.equals("All Statuses") || b.getStatus().equalsIgnoreCase(statusFilter);
            if (matchText && matchStatus) {
                filtered.add(b);
            }
        }
        billTableView.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleViewReceiptDialog() {
        Bill sel = billTableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an invoice from the table to view receipt.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Hospital Billing Statement");
        dialog.setHeaderText("HEALTHDESK OFFICIAL BILLING RECEIPT");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        String receipt =
                "=================================================\n" +
                "               HEALTHDESK CLINIC\n" +
                "         OFFICIAL HOSPITAL INVOICE\n" +
                "=================================================\n" +
                "Invoice Number: #" + sel.getId() + "\n" +
                "Invoice Date:   " + sel.getDate() + "\n" +
                "Patient:        " + sel.getPatientName() + " (ID: " + sel.getPatientId() + ")\n" +
                "Payment Status: " + sel.getStatus().toUpperCase() + "\n" +
                "-------------------------------------------------\n" +
                "ITEMIZED CHARGES                       AMOUNT ($)\n" +
                "-------------------------------------------------\n" +
                String.format("  Consultation Fee:                   %10.2f\n", sel.getConsultationFee()) +
                String.format("  Laboratory Services:                %10.2f\n", sel.getLabFee()) +
                String.format("  Bed / Inpatient Ward:               %10.2f\n", sel.getBedFee()) +
                String.format("  Pharmaceuticals / Medicine:         %10.2f\n", sel.getMedicineFee()) +
                String.format("  Other Hospital Charges:             %10.2f\n", sel.getOtherFee()) +
                "-------------------------------------------------\n" +
                String.format("  TOTAL AMOUNT DUE:                   %10.2f\n", sel.getTotal()) +
                "=================================================\n" +
                "Thank you for choosing HealthDesk Healthcare.\n";

        TextArea area = new TextArea(receipt);
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        area.setPrefSize(480, 360);

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
