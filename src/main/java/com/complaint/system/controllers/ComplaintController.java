package com.complaint.system.controllers;

import com.complaint.system.service.ComplaintService;
import com.complaint.system.util.Session; // 🔹 Added to track persistent user sessions
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ComplaintController implements Initializable {

    @FXML private TextArea complaintInput;
    @FXML private Label statusLabel;
    @FXML private Label movingLabel;
    // 🔹 Removed: fileNameLabel (no longer needed)

    private final ComplaintService complaintService = new ComplaintService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startScrollingText();
        // Reset status on load for a fresh persistent flow
        statusLabel.setText("");
    }

    private void startScrollingText() {
        movingLabel.setTranslateX(1000);
        TranslateTransition transition = new TranslateTransition(Duration.seconds(18), movingLabel);
        transition.setFromX(1000);
        transition.setToX(-1500);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.play();
    }

    // ❌ Removed: handleFileUpload (Image feature deleted)

    @FXML
    public void handleSubmit() {
        String input = complaintInput.getText().trim();
        if (input.isEmpty()) {
            statusLabel.setText("Please describe the issue first.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }
        int userId = Session.getUserId();
        if (userId == 0) {
            statusLabel.setText("No active session. Please re-login.");
            return;
        }
        statusLabel.setText("AI Agent is processing...");
        statusLabel.setStyle("-fx-text-fill: #3498db;");

        boolean isSaved = complaintService.handleNewComplaint(userId, input, null);
        if (isSaved) {
            statusLabel.setText("AI Categorized & Saved to MySQL!");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            complaintInput.clear();
        } else {
            statusLabel.setText("Error: Check Database Connection.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    protected void handleAnalytics() {
        try {
            java.net.http.HttpResponse<String> response = com.complaint.system.util.ApiClient.get(
                    "/api/complaints/user/" + Session.getUserId() + "/analytics");
            if (response.statusCode() == 200) {
                Session.setLastAnalyticsJson(response.body());
            }
        } catch (Exception e) {
            System.err.println("Analytics error: " + e.getMessage());
        }
        navigateTo("/analytics.fxml", "ICRS Analytics");
    }

    @FXML
    protected void handleHistory() {
        try {
            java.net.http.HttpResponse<String> response = com.complaint.system.util.ApiClient.get(
                    "/api/complaints/user/" + Session.getUserId());
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Session.setLastComplaintList(mapper.readValue(response.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<>() {}));
            }
        } catch (Exception e) {
            System.err.println("History error: " + e.getMessage());
        }
        navigateTo("/history.fxml", "ICRS History");
    }

    @FXML
    protected void handleLogout() {
        Session.clear();
        navigateTo("/login.fxml", "ICRS Login");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            if (root == null) throw new Exception("FXML not found: " + fxmlPath);
            javafx.stage.Stage stage = (javafx.stage.Stage) complaintInput.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}