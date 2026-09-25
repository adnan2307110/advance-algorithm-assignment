package com.healthdesk.controller;

import com.healthdesk.Main;
import com.healthdesk.model.User;
import com.healthdesk.service.AuthService;
import com.healthdesk.service.NotificationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Layout controller managing the sidebar navigation, top header,
 * role-based access control, notification badge, and dynamic view swapping.
 */
public class MainLayoutController {

    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnDoctors;
    @FXML private Button btnAppointments;
    @FXML private Button btnEmergency;
    @FXML private Button btnBeds;
    @FXML private Button btnLab;
    @FXML private Button btnPrescriptions;
    @FXML private Button btnBilling;
    @FXML private Button btnHistory;
    @FXML private Button btnHealthInfo;

    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleBadge;
    @FXML private Label pageTitleLabel;
    @FXML private Label headerDateLabel;
    @FXML private Button btnNotifications;

    @FXML private StackPane contentArea;

    private static MainLayoutController instance;
    private Button currentActiveButton;

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        User user = AuthService.getInstance().getCurrentUser();
        if (user != null) {
            userFullNameLabel.setText(user.getFullName());
            userRoleBadge.setText(user.getRole().toUpperCase());
            applyRolePermissions(user.getRole());
        }

        headerDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")));

        // Setup notification listener
        updateNotificationButton();
        NotificationService.getInstance().addListener(msg -> {
            Platform.runLater(this::updateNotificationButton);
        });

        // Load initial Dashboard
        showDashboard();
    }

    private void applyRolePermissions(String role) {
        String r = role.toLowerCase();
        if (r.contains("admin")) {
            // Admin has access to all modules
            return;
        }

        if (r.contains("receptionist")) {
            disableNavButton(btnDoctors, "Doctor management is restricted to Administrators.");
            disableNavButton(btnPrescriptions, "Prescriptions can only be created by attending Physicians.");
        } else if (r.contains("doctor")) {
            disableNavButton(btnDoctors, "Doctor management is restricted to Administrators.");
            disableNavButton(btnBilling, "Billing is managed by Receptionists and Accounting.");
        } else if (r.contains("lab")) {
            disableNavButton(btnPatients, "Access restricted for Lab Technicians.");
            disableNavButton(btnDoctors, "Access restricted for Lab Technicians.");
            disableNavButton(btnAppointments, "Access restricted for Lab Technicians.");
            disableNavButton(btnEmergency, "Access restricted for Lab Technicians.");
            disableNavButton(btnBeds, "Access restricted for Lab Technicians.");
            disableNavButton(btnPrescriptions, "Access restricted for Lab Technicians.");
            disableNavButton(btnBilling, "Access restricted for Lab Technicians.");
        }
    }

    private void disableNavButton(Button btn, String reason) {
        btn.setDisable(true);
        btn.setTooltip(new Tooltip(reason));
        btn.setStyle("-fx-opacity: 0.35;");
    }

    private void updateNotificationButton() {
        int count = NotificationService.getInstance().getNotifications().size();
        btnNotifications.setText("🔔 Notifications (" + count + ")");
    }

    public void loadView(String fxmlPath, String title, Button activeBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            contentArea.getChildren().setAll(node);
            pageTitleLabel.setText(title);

            if (currentActiveButton != null) {
                currentActiveButton.getStyleClass().remove("nav-btn-active");
            }
            if (activeBtn != null) {
                if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
                    activeBtn.getStyleClass().add("nav-btn-active");
                }
                currentActiveButton = activeBtn;
            }
        } catch (IOException e) {
            System.err.println("[MainLayoutController] Failed to load view: " + fxmlPath + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard() {
        loadView("/fxml/dashboard.fxml", "Hospital Operations Dashboard", btnDashboard);
    }

    @FXML public void showPatients() {
        loadView("/fxml/patient.fxml", "Patient Directory & Registration", btnPatients);
    }

    @FXML public void showDoctors() {
        loadView("/fxml/doctor.fxml", "Medical Staff & Doctor Management", btnDoctors);
    }

    @FXML public void showAppointments() {
        loadView("/fxml/appointment.fxml", "Outpatient Appointment Schedule", btnAppointments);
    }

    @FXML public void showEmergency() {
        loadView("/fxml/emergency.fxml", "Emergency Room Triage Queue (PriorityQueue)", btnEmergency);
    }

    @FXML public void showBeds() {
        loadView("/fxml/bed.fxml", "Inpatient Bed & Ward Management", btnBeds);
    }

    @FXML public void showLaboratory() {
        loadView("/fxml/laboratory.fxml", "Laboratory Diagnostics & Multithreaded Processing", btnLab);
    }

    @FXML public void showPrescriptions() {
        loadView("/fxml/prescription.fxml", "Prescription & Pharmacy Management", btnPrescriptions);
    }

    @FXML public void showBilling() {
        loadView("/fxml/billing.fxml", "Patient Billing & Financial Records", btnBilling);
    }

    @FXML public void showHistory() {
        loadView("/fxml/history.fxml", "Unified Patient Medical Dossier & History", btnHistory);
    }

    @FXML public void showHealthInfo() {
        loadView("/fxml/health_info.fxml", "Medical Reference API (HTTP & JSON)", btnHealthInfo);
    }

    @FXML
    private void showNotificationsDialog() {
        List<String> list = NotificationService.getInstance().getNotifications();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("System Notifications & Alerts");
        dialog.setHeaderText("Recent Notifications & Reminders (" + list.size() + ")");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        ListView<String> listView = new ListView<>();
        listView.setPrefSize(480, 320);
        if (list.isEmpty()) {
            listView.getItems().add("No notifications at this time.");
        } else {
            listView.getItems().addAll(list);
        }

        pane.setContent(listView);
        dialog.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuthService.getInstance().logout();
        Main.navigateToLogin();
    }
}
