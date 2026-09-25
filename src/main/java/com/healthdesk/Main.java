package com.healthdesk;

import com.healthdesk.database.Database;
import com.healthdesk.service.LabService;
import com.healthdesk.service.NotificationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application entrypoint for HealthDesk.
 */
public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("HealthDesk – Integrated Healthcare Management System");

        // Initialize Database
        Database.initialize();

        // Start background appointment notification service
        NotificationService.getInstance().start();

        // Launch with Login view
        navigateToLogin();

        primaryStage.show();
    }

    public static void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 650);
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("[Main] Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void navigateToMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main_layout.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("[Main] Failed to load main layout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        // Cleanly terminate background multithreaded services upon window close
        NotificationService.getInstance().stop();
        LabService.getInstance().shutdown();
        System.out.println("[Main] HealthDesk background services terminated cleanly.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
