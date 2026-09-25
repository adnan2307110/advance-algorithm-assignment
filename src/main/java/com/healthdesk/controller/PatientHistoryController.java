package com.healthdesk.controller;

import com.healthdesk.database.*;
import com.healthdesk.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller for Patient Medical History & Dossier module.
 * Aggregates appointments, lab results, prescriptions, bills, and inpatient bed status.
 */
public class PatientHistoryController {

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private Label lblName;
    @FXML private Label lblBloodGroup;
    @FXML private Label lblAgeGender;
    @FXML private Label lblPhone;
    @FXML private Label lblAddress;
    @FXML private Label lblBedInfo;

    // Appointments Tab
    @FXML private TableView<Appointment> appointmentsTableView;
    @FXML private TableColumn<Appointment, String> colApptDate;
    @FXML private TableColumn<Appointment, String> colApptTime;
    @FXML private TableColumn<Appointment, String> colApptDoctor;
    @FXML private TableColumn<Appointment, String> colApptStatus;

    // Lab Tests Tab
    @FXML private TableView<LabTest> labTableView;
    @FXML private TableColumn<LabTest, String> colLabDate;
    @FXML private TableColumn<LabTest, String> colLabTest;
    @FXML private TableColumn<LabTest, String> colLabDoctor;
    @FXML private TableColumn<LabTest, String> colLabStatus;
    @FXML private TableColumn<LabTest, String> colLabResult;

    // Prescriptions Tab
    @FXML private TableView<Prescription> prescriptionsTableView;
    @FXML private TableColumn<Prescription, String> colRxDate;
    @FXML private TableColumn<Prescription, String> colRxDoctor;
    @FXML private TableColumn<Prescription, String> colRxMedicine;
    @FXML private TableColumn<Prescription, String> colRxDosage;
    @FXML private TableColumn<Prescription, String> colRxDuration;
    @FXML private TableColumn<Prescription, String> colRxInstructions;

    // Bills Tab
    @FXML private TableView<Bill> billsTableView;
    @FXML private TableColumn<Bill, Integer> colBillId;
    @FXML private TableColumn<Bill, String> colBillDate;
    @FXML private TableColumn<Bill, String> colBillConsult;
    @FXML private TableColumn<Bill, String> colBillLab;
    @FXML private TableColumn<Bill, String> colBillBed;
    @FXML private TableColumn<Bill, String> colBillMed;
    @FXML private TableColumn<Bill, String> colBillTotal;
    @FXML private TableColumn<Bill, String> colBillStatus;

    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final LabTestDAO labTestDAO = new LabTestDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final BillDAO billDAO = new BillDAO();
    private final BedDAO bedDAO = new BedDAO();

    private Patient selectedPatient;

    @FXML
    public void initialize() {
        setupPatientConverter();
        setupTableColumns();
        loadPatients();
    }

    private void setupPatientConverter() {
        patientCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getName() + " (ID: " + p.getId() + ", " + p.getBloodGroup() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });
    }

    private void setupTableColumns() {
        // Appointments
        colApptDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colApptTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colApptDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colApptStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Lab
        colLabDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLabTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colLabDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colLabStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLabResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        // Prescriptions
        colRxDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colRxDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colRxMedicine.setCellValueFactory(new PropertyValueFactory<>("medicine"));
        colRxDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colRxDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRxInstructions.setCellValueFactory(new PropertyValueFactory<>("instructions"));

        // Bills
        colBillId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBillDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colBillConsult.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getConsultationFee())));
        colBillLab.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getLabFee())));
        colBillBed.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getBedFee())));
        colBillMed.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getMedicineFee())));
        colBillTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getTotal())));
        colBillStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void loadPatients() {
        List<Patient> patients = patientDAO.getAllPatients();
        patientCombo.setItems(FXCollections.observableArrayList(patients));
        if (!patients.isEmpty() && selectedPatient == null) {
            patientCombo.getSelectionModel().selectFirst();
            handlePatientSelected();
        }
    }

    @FXML
    private void handlePatientSelected() {
        selectedPatient = patientCombo.getValue();
        if (selectedPatient == null) return;

        // Demographics
        lblName.setText(selectedPatient.getName());
        lblBloodGroup.setText(selectedPatient.getBloodGroup());
        lblAgeGender.setText("Age: " + selectedPatient.getAge() + " | Gender: " + selectedPatient.getGender());
        lblPhone.setText("Phone: " + selectedPatient.getPhone());
        lblAddress.setText("Address: " + (selectedPatient.getAddress().isEmpty() ? "N/A" : selectedPatient.getAddress()));

        // Inpatient bed
        Bed bed = bedDAO.getBedByPatient(selectedPatient.getId());
        if (bed != null) {
            lblBedInfo.setText("Bed " + bed.getId() + " (" + bed.getWard() + " Ward)");
        } else {
            lblBedInfo.setText("Not Currently Admitted");
        }

        // Appointments
        List<Appointment> appts = appointmentDAO.getAppointmentsByPatient(selectedPatient.getId());
        appointmentsTableView.setItems(FXCollections.observableArrayList(appts));

        // Lab tests
        List<LabTest> tests = labTestDAO.getTestsByPatient(selectedPatient.getId());
        labTableView.setItems(FXCollections.observableArrayList(tests));

        // Prescriptions
        List<Prescription> rxs = prescriptionDAO.getPrescriptionsByPatient(selectedPatient.getId());
        prescriptionsTableView.setItems(FXCollections.observableArrayList(rxs));

        // Bills
        List<Bill> bills = billDAO.getBillsByPatient(selectedPatient.getId());
        billsTableView.setItems(FXCollections.observableArrayList(bills));
    }

    @FXML
    private void handlePrintDossier() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a patient first.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("                  HEALTHDESK MEDICAL DOSSIER                     \n");
        sb.append("=================================================================\n");
        sb.append("Patient ID:    ").append(selectedPatient.getId()).append("\n");
        sb.append("Patient Name:  ").append(selectedPatient.getName()).append("\n");
        sb.append("Age / Gender:  ").append(selectedPatient.getAge()).append(" yrs / ").append(selectedPatient.getGender()).append("\n");
        sb.append("Blood Group:   ").append(selectedPatient.getBloodGroup()).append("\n");
        sb.append("Phone:         ").append(selectedPatient.getPhone()).append("\n");
        sb.append("Address:       ").append(selectedPatient.getAddress()).append("\n");
        sb.append("Inpatient Bed: ").append(lblBedInfo.getText()).append("\n");
        sb.append("-----------------------------------------------------------------\n");

        sb.append("\n[ APPOINTMENTS HISTORY ]\n");
        List<Appointment> appts = appointmentDAO.getAppointmentsByPatient(selectedPatient.getId());
        if (appts.isEmpty()) {
            sb.append("  No previous appointments found.\n");
        } else {
            for (Appointment a : appts) {
                sb.append(String.format("  * %s %-6s | %-20s | %s\n", a.getDate(), a.getTime(), a.getDoctorName(), a.getStatus()));
            }
        }

        sb.append("\n[ LABORATORY DIAGNOSTICS & FINDINGS ]\n");
        List<LabTest> tests = labTestDAO.getTestsByPatient(selectedPatient.getId());
        if (tests.isEmpty()) {
            sb.append("  No laboratory tests on file.\n");
        } else {
            for (LabTest t : tests) {
                sb.append("  * ").append(t.getDate()).append(" | ").append(t.getTestName()).append(" (").append(t.getStatus()).append(")\n");
                if (t.getResult() != null && !t.getResult().isEmpty()) {
                    sb.append("    Findings: ").append(t.getResult().replace("\n", "\n    ")).append("\n");
                }
            }
        }

        sb.append("\n[ ACTIVE MEDICATIONS & PRESCRIPTIONS ]\n");
        List<Prescription> rxs = prescriptionDAO.getPrescriptionsByPatient(selectedPatient.getId());
        if (rxs.isEmpty()) {
            sb.append("  No medications prescribed.\n");
        } else {
            for (Prescription p : rxs) {
                sb.append(String.format("  * %s | %-16s | %-10s | %s (%s)\n",
                        p.getDate(), p.getMedicine(), p.getDosage(), p.getDuration(), p.getInstructions()));
            }
        }

        sb.append("\n[ FINANCIAL INVOICES & BILLS ]\n");
        List<Bill> bills = billDAO.getBillsByPatient(selectedPatient.getId());
        double totalBilled = 0;
        if (bills.isEmpty()) {
            sb.append("  No billing records found.\n");
        } else {
            for (Bill b : bills) {
                totalBilled += b.getTotal();
                sb.append(String.format("  * Invoice #%-4d | %s | Total: $%8.2f | Status: %s\n",
                        b.getId(), b.getDate(), b.getTotal(), b.getStatus()));
            }
            sb.append(String.format("  Cumulative Lifetime Billing: $%.2f\n", totalBilled));
        }
        sb.append("=================================================================\n");
        sb.append("Generated by HealthDesk Hospital Information System\n");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Unified Patient Medical Dossier");
        dialog.setHeaderText("Complete Patient Medical Record - " + selectedPatient.getName());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        area.setPrefSize(640, 480);

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
