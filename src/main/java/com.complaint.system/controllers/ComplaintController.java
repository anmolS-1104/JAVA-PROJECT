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
    // 🔹 Removed: fileNameLabel

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

    //  Removed: handleFileUpload (Image feature deleted)

    @FXML
    public void handleSubmit(ActionEvent event) {
        String input = complaintInput.getText().trim();

        // 1. Validation for Persistent Flow
        if (input.isEmpty()) {
            statusLabel.setText("⚠ Please describe the issue first.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }

        statusLabel.setText(" AI Agent is processing...");
        statusLabel.setStyle("-fx-text-fill: #3498db;");

        // 2. Persistent Session Check
        int userId = Session.getUserId();
        if (userId == 0) {
            statusLabel.setText(" Error: No active session. Please re-login.");
            return;
        }

        // 3. Process Complaint (Image path is now hardcoded as null or empty)
        boolean isSaved = complaintService.handleNewComplaint(userId, input, null);

        if (isSaved) {
            statusLabel.setText("AI Categorized & Saved to MySQL!");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // 4. UI Reset for Persistent usage (ready for next complaint)
            complaintInput.clear();
        } else {
            statusLabel.setText(" Error: Check Database Connection.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}