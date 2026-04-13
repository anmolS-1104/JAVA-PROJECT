package com.complaint.system;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;

public class ComplaintController {

    // 1. These variables MUST match the fx:id in your dashboard.fxml
    @FXML
    private TextArea complaintInput;

    @FXML
    private Label statusLabel;

    @FXML
    private Label fileNameLabel; // New Label for file path display

    /**
     * Handles the file attachment logic
     */
    @FXML
    public void handleFileUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach Evidence (Image/PDF)");

        // Filter for specific file types
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx")
        );

        // Open the dialog window
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            fileNameLabel.setText("Attached: " + selectedFile.getName());
            fileNameLabel.setStyle("-fx-text-fill: #2980b9;");
        } else {
            fileNameLabel.setText("No file selected");
        }
    }

    /**
     * Handles the AI submission logic
     */
    @FXML
    public void handleSubmit(ActionEvent event) {
        String userInput = complaintInput.getText();

        if (userInput == null || userInput.trim().isEmpty()) {
            statusLabel.setText("Status: Error! Input cannot be empty.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }

        // --- Simulated Agentic AI Logic ---
        // We detect keywords to simulate "Intelligent" routing
        if (userInput.toLowerCase().contains("urgent") || userInput.toLowerCase().contains("emergency")) {
            statusLabel.setText("Status: High Priority Detected. Sent to Senior Agent.");
            statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        } else if (userInput.toLowerCase().contains("payment") || userInput.toLowerCase().contains("refund")) {
            statusLabel.setText("Status: Financial Query Detected. Routed to Billing.");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            statusLabel.setText("Status: Complaint logged successfully.");
            statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green for success
        }

        // Future Step: Call your DBConnection.java here to save the 'userInput'
    }
}