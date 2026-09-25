package com.healthdesk.controller;

import com.healthdesk.Main;
import com.healthdesk.service.AuthService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the Login screen.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Admin", "Receptionist", "Doctor", "Lab Technician"
        ));
        roleComboBox.getSelectionModel().select("Admin");
        usernameField.setText("admin");
        passwordField.setText("admin123");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        boolean success = AuthService.getInstance().login(username, password, role);
        if (success) {
            errorLabel.setVisible(false);
            Main.navigateToMainLayout();
        } else {
            showError("Invalid username, password, or role selection. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void fillAdmin() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        roleComboBox.getSelectionModel().select("Admin");
        errorLabel.setVisible(false);
    }

    @FXML
    private void fillReceptionist() {
        usernameField.setText("reception");
        passwordField.setText("rec123");
        roleComboBox.getSelectionModel().select("Receptionist");
        errorLabel.setVisible(false);
    }

    @FXML
    private void fillDoctor() {
        usernameField.setText("doctor");
        passwordField.setText("doc123");
        roleComboBox.getSelectionModel().select("Doctor");
        errorLabel.setVisible(false);
    }

    @FXML
    private void fillLab() {
        usernameField.setText("lab");
        passwordField.setText("lab123");
        roleComboBox.getSelectionModel().select("Lab Technician");
        errorLabel.setVisible(false);
    }
}
