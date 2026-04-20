package com.complaint.system.controllers;

import com.complaint.system.service.ComplaintService;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ComplaintController implements Initializable {

    @FXML private TextArea complaintInput;
    @FXML private Label statusLabel;
    @FXML private Label fileNameLabel;
    @FXML private Label movingLabel;

    private String currentFilePath = "";

    // 🔹 New: Connection to the Service layer
    private ComplaintService complaintService = new ComplaintService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startScrollingText();
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

    @FXML
    public void handleFileUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            currentFilePath = selectedFile.getAbsolutePath();
            fileNameLabel.setText("✔ Attached: " + selectedFile.getName());
        } else {
            fileNameLabel.setText("No file selected");
            currentFilePath = "";
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        String input = complaintInput.getText();

        if (input == null || input.trim().isEmpty()) {
            statusLabel.setText("⚠ Please describe the issue first.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }

        statusLabel.setText("🤖 AI Agent is processing...");
        statusLabel.setStyle("-fx-text-fill: #3498db;");

        // 🔹 FIX: Call the Service (which handles Classification -> DBMS)
        boolean isSaved = complaintService.handleNewComplaint(input, currentFilePath);

        if (isSaved) {
            statusLabel.setText("✅ AI Categorized & Saved to MySQL!");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // Clear UI
            complaintInput.clear();
            fileNameLabel.setText("No file attached");
            currentFilePath = "";
        } else {
            statusLabel.setText("❌ Error: Check Database Connection.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}