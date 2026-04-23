package com.complaint.system.controllers;

import com.complaint.system.util.ApiClient;
import com.complaint.system.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            // Build JSON safely using Jackson instead of raw string interpolation
            ObjectNode payload = mapper.createObjectNode();
            payload.put("email", email);
            payload.put("password", password);
            payload.put("role", role);
            String jsonPayload = mapper.writeValueAsString(payload);

            // Fixed endpoint: was /login, now /api/auth/login
            HttpResponse<String> response = ApiClient.post("/api/auth/login", jsonPayload);

            if (response.statusCode() == 200) {
                JsonNode user = mapper.readTree(response.body());

                Session.saveSession(
                        user.get("id").asInt(),
                        user.get("fullName").asText(),
                        user.get("role").asText(),
                        user.has("department") && !user.get("department").isNull()
                                ? user.get("department").asText() : null
                );

                Platform.runLater(() -> {
                    try {
                        // Fixed routing: agents go to agent_dashboard, customers go to dashboard
                        String fxmlPath = "AGENT".equalsIgnoreCase(role)
                                ? "/agent_dashboard.fxml"
                                : "/dashboard.fxml";

                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();

                        Stage stage = (Stage) emailField.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("ICRS - " + role);
                        stage.centerOnScreen();
                        stage.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Could not load dashboard.");
                    }
                });

            } else {
                showError("Invalid credentials or role mismatch.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Connection error: is the backend running?");
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