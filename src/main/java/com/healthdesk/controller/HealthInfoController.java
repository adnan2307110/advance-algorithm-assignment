package com.healthdesk.controller;

import com.healthdesk.api.HealthAPI;
import com.healthdesk.model.HealthInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for External Health Information API & JSON module.
 * Executes HTTP requests and JSON parsing in background threads to keep UI fluid.
 */
public class HealthInfoController {

    @FXML private Button btnFetchApi;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private Label lblStatus;

    @FXML private TableView<HealthInfo> topicsTableView;
    @FXML private TableColumn<HealthInfo, String> colName;
    @FXML private TableColumn<HealthInfo, String> colRange;
    @FXML private TableColumn<HealthInfo, String> colCategory;

    @FXML private Label lblDetailName;
    @FXML private Label lblDetailCategory;
    @FXML private Label lblDetailRange;
    @FXML private TextArea txtDescription;

    private final HealthAPI healthAPI = new HealthAPI();
    private final ObservableList<HealthInfo> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryFilterCombo.setItems(FXCollections.observableArrayList("All Categories"));
        categoryFilterCombo.getSelectionModel().selectFirst();

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRange.setCellValueFactory(new PropertyValueFactory<>("normalRange"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        topicsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateDetails(newVal);
            }
        });

        // Fetch topics automatically on initial load
        handleFetchApi();
    }

    @FXML
    private void handleFetchApi() {
        progressIndicator.setVisible(true);
        btnFetchApi.setDisable(true);
        lblStatus.setText("Executing HTTP Request & JSON Parsing in background...");

        healthAPI.fetchHealthInfoAsync().thenAccept(topics -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnFetchApi.setDisable(false);
                masterList.clear();
                masterList.addAll(topics);

                // Populate category filter options
                Set<String> categories = new HashSet<>();
                categories.add("All Categories");
                for (HealthInfo hi : topics) {
                    if (hi.getCategory() != null && !hi.getCategory().isEmpty()) {
                        categories.add(hi.getCategory());
                    }
                }
                categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
                categoryFilterCombo.getSelectionModel().select("All Categories");

                applyFilter();
                lblStatus.setText("Successfully loaded " + topics.size() + " topics from API.");

                if (!topics.isEmpty()) {
                    topicsTableView.getSelectionModel().selectFirst();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnFetchApi.setDisable(false);
                lblStatus.setText("Failed to fetch API data: " + ex.getMessage());
            });
            return null;
        });
    }

    private void populateDetails(HealthInfo info) {
        lblDetailName.setText(info.getName());
        lblDetailCategory.setText("Category: " + info.getCategory());
        lblDetailRange.setText(info.getNormalRange());
        txtDescription.setText(info.getDescription());
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }

    @FXML
    private void handleFilterCategory() {
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String cat = categoryFilterCombo.getValue();
        if (cat == null) cat = "All Categories";

        List<HealthInfo> filtered = new ArrayList<>();
        for (HealthInfo hi : masterList) {
            boolean matchesSearch = query.isEmpty() ||
                    hi.getName().toLowerCase().contains(query) ||
                    hi.getNormalRange().toLowerCase().contains(query) ||
                    hi.getDescription().toLowerCase().contains(query);

            boolean matchesCategory = cat.equals("All Categories") || hi.getCategory().equalsIgnoreCase(cat);

            if (matchesSearch && matchesCategory) {
                filtered.add(hi);
            }
        }
        topicsTableView.setItems(FXCollections.observableArrayList(filtered));
    }
}
