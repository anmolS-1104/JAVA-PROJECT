package com.complaint.system.controllers;

import com.complaint.system.util.ApiClient;
import com.complaint.system.util.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.complaint.system.dto.ComplaintDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.http.HttpResponse;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.ChoiceDialog;
import java.util.Optional;

public class AgentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalCountLabel;
    @FXML private TableView<ComplaintDTO> complaintsTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idCol;
    @FXML private TableColumn<ComplaintDTO, String> descCol;
    @FXML private TableColumn<ComplaintDTO, String> statusCol;
    @FXML private Button updateStatusBtn;

    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<ComplaintDTO> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getUserName());

        // Map Table Columns to ComplaintDTO properties
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        complaintsTable.setItems(tableData);
        loadComplaints();
    }

    private void loadComplaints() {
        try {
            // Use the stored department to fetch specific complaints
            String endpoint = "/api/complaints/department/" + Session.getUserDepartment();
            HttpResponse<String> response = ApiClient.get(endpoint);

            if (response.statusCode() == 200) {
                List<ComplaintDTO> complaints = mapper.readValue(
                        response.body(),
                        new TypeReference<List<ComplaintDTO>>() {}
                );

                tableData.setAll(complaints);
                totalCountLabel.setText(String.valueOf(complaints.size()));
            }
        } catch (Exception e) {
            System.err.println("Failed to load complaints: " + e.getMessage());
        }
    }

    @FXML
    protected void handleUpdateStatus() {
        ComplaintDTO selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        List<String> choices = List.of("Pending", "In Progress", "Resolved");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus() != null ? selected.getStatus() : "Pending", choices);
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update status for Complaint ID: " + selected.getId());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String payload = String.format("{\"status\": \"%s\"}", result.get());
                HttpResponse<String> response = ApiClient.put("/api/complaints/" + selected.getId() + "/status", payload);

                if (response.statusCode() == 200) {
                    loadComplaints();
                }
            } catch (Exception e) {
                System.err.println("Failed to update status: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleDelete() {
        ComplaintDTO selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            HttpResponse<String> response = ApiClient.delete("/api/complaints/" + selected.getId());
            if (response.statusCode() == 200) {
                loadComplaints();
            }
        } catch (Exception e) {
            System.err.println("Failed to delete complaint: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 650));
            stage.setTitle("ICRS - Login");
            stage.show();
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }
}