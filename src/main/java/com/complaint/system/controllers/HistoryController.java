package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class HistoryController {

    @FXML private TableView<ComplaintDTO> historyTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idCol;
    @FXML private TableColumn<ComplaintDTO, String> descCol;
    @FXML private TableColumn<ComplaintDTO, String> deptCol;
    @FXML private TableColumn<ComplaintDTO, String> priorityCol;
    @FXML private TableColumn<ComplaintDTO, String> statusCol;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        List<ComplaintDTO> complaints = Session.getLastComplaintList();
        if (complaints != null) {
            historyTable.setItems(FXCollections.observableArrayList(complaints));
        }
    }

    @FXML
    protected void handleDashboard() {
        navigateTo("/dashboard.fxml", "ICRS - Dashboard");
    }

    @FXML
    protected void handleAnalytics() {
        try {
            com.complaint.system.util.ApiClient.get(
                    "/api/complaints/user/" + Session.getUserId() + "/analytics"
            );
            navigateTo("/analytics.fxml", "ICRS - Analytics");
        } catch (Exception e) {
            System.err.println("Error loading analytics: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        Session.clear();
        navigateTo("/login.fxml", "ICRS - Login");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) historyTable.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}