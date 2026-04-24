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
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AgentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalCountLabel;
    @FXML private TableView<ComplaintDTO> complaintsTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idCol;
    @FXML private TableColumn<ComplaintDTO, String> descCol;
    @FXML private TableColumn<ComplaintDTO, String> statusCol;
    @FXML private TableColumn<ComplaintDTO, String> priorityCol;
    @FXML private TableColumn<ComplaintDTO, String> notesCol;
    @FXML private Button updateStatusBtn;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> sortByFilter;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<ComplaintDTO> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getUserName());

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        complaintsTable.setItems(tableData);

        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "In Progress", "Resolved"));
        priorityFilter.setItems(FXCollections.observableArrayList("All", "HIGH", "MEDIUM", "NORMAL"));
        sortByFilter.setItems(FXCollections.observableArrayList("Default", "date", "priority"));

        loadComplaints();
    }

    private void loadComplaints() {
        try {
            String endpoint = "/api/complaints/department/" + Session.getUserDepartment();
            HttpResponse<String> response = ApiClient.get(endpoint);
            if (response.statusCode() == 200) {
                List<ComplaintDTO> complaints = mapper.readValue(
                        response.body(), new TypeReference<List<ComplaintDTO>>() {});
                tableData.setAll(complaints);
                totalCountLabel.setText(String.valueOf(complaints.size()));
            }
        } catch (Exception e) {
            System.err.println("Failed to load complaints: " + e.getMessage());
        }
    }

    @FXML
    protected void handleFilter() {
        try {
            String status   = resolveFilter(statusFilter.getValue());
            String priority = resolveFilter(priorityFilter.getValue());
            String sortBy   = resolveFilter(sortByFilter.getValue());
            String dept     = Session.getUserDepartment();

            StringBuilder url = new StringBuilder("/api/complaints/filter?department=" + dept);
            if (status   != null) url.append("&status=").append(status);
            if (priority != null) url.append("&priority=").append(priority);
            if (sortBy   != null) url.append("&sortBy=").append(sortBy);

            HttpResponse<String> response = ApiClient.get(url.toString());
            if (response.statusCode() == 200) {
                List<ComplaintDTO> complaints = mapper.readValue(
                        response.body(), new TypeReference<List<ComplaintDTO>>() {});
                tableData.setAll(complaints);
                totalCountLabel.setText(String.valueOf(complaints.size()));
            }
        } catch (Exception e) {
            System.err.println("Filter error: " + e.getMessage());
        }
    }

    @FXML
    protected void handleResetFilter() {
        statusFilter.setValue(null);
        priorityFilter.setValue(null);
        sortByFilter.setValue(null);
        loadComplaints();
    }

    @FXML
    protected void handleUpdateStatus() {
        ComplaintDTO selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        List<String> choices = List.of("Pending", "In Progress", "Resolved");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                selected.getStatus() != null ? selected.getStatus() : "Pending", choices);
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Complaint ID: " + selected.getId());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String payload = String.format("{\"status\": \"%s\"}", result.get());
                HttpResponse<String> response = ApiClient.put(
                        "/api/complaints/" + selected.getId() + "/status", payload);
                if (response.statusCode() == 200) loadComplaints();
            } catch (Exception e) {
                System.err.println("Failed to update status: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleUpdateNotes() {
        ComplaintDTO selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(
                selected.getNotes() != null ? selected.getNotes() : "");
        dialog.setTitle("Add / Edit Notes");
        dialog.setHeaderText("Complaint ID: " + selected.getId());
        dialog.setContentText("Resolution notes:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String payload = String.format("{\"notes\": \"%s\"}",
                        result.get().replace("\"", "\\\""));
                HttpResponse<String> response = ApiClient.put(
                        "/api/complaints/" + selected.getId() + "/notes", payload);
                if (response.statusCode() == 200) loadComplaints();
            } catch (Exception e) {
                System.err.println("Failed to update notes: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleDelete() {
        ComplaintDTO selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            HttpResponse<String> response = ApiClient.delete(
                    "/api/complaints/" + selected.getId());
            if (response.statusCode() == 200) loadComplaints();
        } catch (Exception e) {
            System.err.println("Failed to delete: " + e.getMessage());
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

    private String resolveFilter(String value) {
        if (value == null || value.equals("All") || value.equals("Default")) return null;
        return value;
    }
}