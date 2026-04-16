package com.complaint.system;// It should now look like this:
import com.complaint.system.dao.ComplaintDAOImpl;


import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
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
    @FXML private Pane tickerPane;

    private String currentFilePath = "";
    private ComplaintDAOImpl complaintDAO = new ComplaintDAOImpl();

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
        fileChooser.setTitle("Attach Evidence");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            currentFilePath = selectedFile.getAbsolutePath();
            fileNameLabel.setText("✔ Attached: " + selectedFile.getName());
            fileNameLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        } else {
            fileNameLabel.setText("No file selected");
            currentFilePath = "";
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        String input = complaintInput.getText();

        // 1. Validation
        if (input == null || input.trim().isEmpty()) {
            statusLabel.setText("⚠ Please describe the issue first.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }

        // 2. Simple AI Categorization
        String aiStatus = "PENDING";
        if (input.toLowerCase().contains("urgent")) {
            aiStatus = "CRITICAL";
            statusLabel.setText("🚀 AI Agent: High Priority detected!");
            statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("🤖 AI Agent: Logging request...");
            statusLabel.setStyle("-fx-text-fill: #2980b9;");
        }

        // 3. Direct Connection to DAO (Pass strings directly)
        boolean isSaved = complaintDAO.submitComplaint(input, currentFilePath, aiStatus);

        if (isSaved) {
            statusLabel.setText("✅ Success! Complaint saved to MySQL Database.");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // Clear UI after success
            complaintInput.clear();
            fileNameLabel.setText("No file attached");
            currentFilePath = "";
        } else {
            statusLabel.setText("❌ Database Error: Connection failed.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}