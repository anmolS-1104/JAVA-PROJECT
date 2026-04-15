package com.complaint.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        roleComboBox.getItems().addAll("CUSTOMER", "AGENT");
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
            String payload = String.format("{\"email\":\"%s\", \"password\":\"%s\", \"role\":\"%s\"}",
                    email, password, role);

            HttpResponse<String> response = ApiClient.post("/api/auth/login", payload);

            if (response.statusCode() == 200) {
                JsonNode user = mapper.readTree(response.body());

                // Safety check: Ensure the response actually has JSON data before reading
                if (user == null || !user.has("id")) {
                    showError("Invalid response from server.");
                    return;
                }

                String department = user.has("department") && !user.get("department").isNull()
                        ? user.get("department").asText() : null;

                Session.saveSession(
                        user.get("id").asInt(),
                        user.get("name").asText(),
                        user.get("role").asText(),
                        department
                );

                if ("AGENT".equals(role)) {
                    System.out.println("Redirecting to Agent Dashboard...");
                } else {
                    System.out.println("Redirecting to Customer Dashboard...");
                }
            } else {
                showError(response.body().isEmpty() ? "Login failed" : response.body());
            }
        } catch (Exception e) {
            // This prints the exact line of the error in your IntelliJ console!
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Check IntelliJ console for details.";
            showError("Connection error: " + errorMsg);
        }
    }

    // NEW: Method to handle the scene switch when the hyperlink is clicked
    @FXML
    protected void handleRegisterRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load registration page.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}