package com.complaint.system.controllers;

import com.complaint.system.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import com.complaint.system.util.ApiClient;
import com.complaint.system.dto.ComplaintDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.HttpResponse;
import java.util.List;

public class AnalyticsController {

    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label financeLabel;
    @FXML private Label logisticsLabel;
    @FXML private Label technicalLabel;

    @FXML
    public void initialize() {
        String json = Session.getLastAnalyticsJson();
        if (json != null && !json.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode data = mapper.readTree(json);

                totalLabel.setText(data.get("total").asText());
                pendingLabel.setText(data.get("pending").asText());
                inProgressLabel.setText(data.get("inProgress").asText());
                resolvedLabel.setText(data.get("resolved").asText());
                financeLabel.setText(data.get("finance").asText());
                logisticsLabel.setText(data.get("logistics").asText());
                technicalLabel.setText(data.get("technical").asText());
            } catch (Exception e) {
                System.err.println("Error parsing analytics JSON: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleDashboard() {
        navigateTo("/dashboard.fxml", "ICRS Dashboard");
    }

    @FXML
    protected void handleHistory() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/complaints/user/" + Session.getUserId());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                List<ComplaintDTO> complaints = mapper.readValue(
                        response.body(), new TypeReference<List<ComplaintDTO>>() {}
                );
                Session.setLastComplaintList(complaints);
                navigateTo("/history.fxml", "ICRS History");
            }
        } catch (Exception e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        Session.clear();
        navigateTo("/login.fxml", "ICRS Login");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}