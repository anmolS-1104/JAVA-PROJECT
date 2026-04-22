package com.complaint.system.controllers;

import com.complaint.system.util.ApiClient;
import com.complaint.system.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("CUSTOMER", "AGENT");
        }
    }

    @FXML
    protected void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            // 1. Prepare JSON payload
            String payload = String.format("{\"email\":\"%s\", \"password\":\"%s\", \"role\":\"%s\"}",
                    email, password, role);

            // 2. Call Backend (Make sure Spring Boot is running!)
            HttpResponse<String> response = ApiClient.post("/login", payload);

            if (response.statusCode() == 200) {
                JsonNode user = mapper.readTree(response.body());

                // 3. Save user data to Session for use in the Dashboard
                Session.saveSession(
                        user.get("id").asInt(),
                        user.get("fullName").asText(),
                        user.get("role").asText(),
                        user.has("department") && !user.get("department").isNull() ? user.get("department").asText() : null
                );

                System.out.println("✅ Login Success! Opening Dashboard...");

                // 4. THE WINDOW SWITCHING CODE
                Platform.runLater(() -> {
                    try {
                        // 🔹 Updated to match your screenshot: "dashboard.fxml"
                        String fxmlPath = "/dashboard.fxml";

                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();

                        // Get current stage and swap the scene
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("ICRS Dashboard - " + role);
                        stage.centerOnScreen();
                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("FXML Error: Could not find dashboard.fxml in resources.");
                    }
                });

            } else {
                showError("Invalid credentials or role mismatch.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Connection error: Is the Backend running?");
        }
    }

    @FXML
    protected void handleRegisterRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Could not load registration page.");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
}