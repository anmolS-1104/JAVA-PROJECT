package com.complaint.system;

import javafx.fxml.FXML;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This starts the "Creators" line moving as soon as the app opens
        startScrollingText();
    }

    private void startScrollingText() {
        // Start position: Off-screen to the right
        movingLabel.setTranslateX(1000);

        TranslateTransition transition = new TranslateTransition();
        transition.setNode(movingLabel);
        transition.setDuration(Duration.seconds(15)); // Adjust speed here (higher = slower)

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