package com.complaint.system.controllers;

import com.complaint.system.util.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.http.HttpResponse;
import java.io.IOException;

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

        // 🔹 Basic Validation
        if (name.isEmpty() || !email.contains("@") || password.length() < 6) {
            showMessage("Invalid details. Email must be valid & Password > 6 chars.", true);
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        try {
            // 🔹 JSON Payload matching your Backend User Entity
            String payload = String.format(
                    "{\"fullName\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"phone\":\"%s\", \"role\":\"CUSTOMER\"}",
                    name, email, password, phone
            );

            // 🔹 Sending to Backend -> MySQL
            HttpResponse<String> response = ApiClient.post("/api/users/register", payload);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                showMessage("Account created! Redirecting to login...", false);
                // Pause slightly so user can see success message before redirect
                handleBackToLogin();
            } else {
                showMessage("Registration failed: " + response.body(), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Connection error: Is the backend server running?", true);
        }
    }

    @FXML
    protected void handleBackToLogin() {
        navigateTo("/login.fxml", "Login - ICRS System", 700, 650);
    }

    // 🔹 Reusable Navigation Method
    private void navigateTo(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading: " + fxmlPath, true);
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
        messageLabel.setVisible(true);
    }
}