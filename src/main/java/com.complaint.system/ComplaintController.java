package com.complaint.system;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;

@Controller
public class ComplaintController {

    // 1. These variables MUST match the fx:id in your dashboard.fxml
    @FXML
    private TextArea complaintInput;

    @FXML
    private Label statusLabel;

    // 2. This method MUST match the onAction="#handleSubmit" in your FXML
    @FXML
    public void handleSubmit(ActionEvent event) {
        String userInput = complaintInput.getText();

        if (userInput.isEmpty()) {
            statusLabel.setText("Status: Error! Please type a complaint.");
            statusLabel.setStyle("-fx-text-fill: #e67e22;"); // Orange for warning
            return;
        }

        // 3. Agentic AI Logic (Example)
        if (userInput.toLowerCase().contains("urgent")) {
            statusLabel.setText("Status: Priority detected! Saved to Database.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red for priority
        } else {
            statusLabel.setText("Status: Complaint logged successfully.");
            statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green for success
        }

        // Future Step: Call your DBConnection.java here to save the 'userInput'
    }
}