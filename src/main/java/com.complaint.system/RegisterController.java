package com.complaint.system;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.http.HttpResponse;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || !email.contains("@") || password.length() < 6) {
            showMessage("Invalid input details.", true);
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        try {
            String payload = String.format(
                    "{\"fullName\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"phone\":\"%s\"}",
                    name, email, password, phone
            );

            HttpResponse<String> response = ApiClient.post("/api/users/register", payload);

            if (response.statusCode() == 200) {
                System.out.println("Account created! Redirecting...");
                handleBackToLogin();
            } else {
                showMessage("Registration failed: " + response.body(), true);
            }
        } catch (Exception e) {
            showMessage("Connection error: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void handleBackToLogin() {
        try {
            // Load the login screen
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/login.fxml"));
            // Get the current window using any existing field
            javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();

            // RIGHT HERE: Set the new larger scene dimensions for the dark theme
            stage.setScene(new javafx.scene.Scene(root, 700, 650));

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Could not load login page.", true);
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        messageLabel.setVisible(true);
    }
}