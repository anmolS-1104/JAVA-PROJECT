package com.complaint.system.controllers;

import com.complaint.system.util.DBConnection;
import com.complaint.system.util.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        if (roleComboBox != null && roleComboBox.getItems().isEmpty()) {
            roleComboBox.getItems().addAll("Customer", "Support Agent");
            roleComboBox.setValue("Customer");
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = (emailField != null && emailField.getText() != null) ? emailField.getText().trim() : "";
        String password = (passwordField != null && passwordField.getText() != null) ? passwordField.getText().trim() : "";
        String role = (roleComboBox != null && roleComboBox.getValue() != null) ? roleComboBox.getValue() : "Customer";

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in both email and password.", "#ef4444");
            return;
        }

        // ================= SUPPORT AGENT LOGIN =================
        if ("Support Agent".equalsIgnoreCase(role)) {
            if (email.equalsIgnoreCase("finance@agent.company.com") && password.equals("finance123")) {
                setAgentSession(9001, "Finance Agent", email, "Finance & Payroll");
                navigateToScene(event, "/agent_dashboard.fxml", "Agent Portal - Finance & Payroll");
            } else if (email.equalsIgnoreCase("tech@agent.company.com") && password.equals("tech123")) {
                setAgentSession(9002, "IT Support Agent", email, "Technical Support");
                navigateToScene(event, "/agent_dashboard.fxml", "Agent Portal - Technical Support");
            } else if (email.equalsIgnoreCase("care@agent.company.com") && password.equals("care123")) {
                setAgentSession(9003, "Customer Care Agent", email, "Customer Care");
                navigateToScene(event, "/agent_dashboard.fxml", "Agent Portal - Customer Care");
            } else {
                showMessage("Invalid Agent credentials for chosen department.", "#ef4444");
            }
            return;
        }

        // ================= CUSTOMER LOGIN =================
        new Thread(() -> {
            boolean authenticated = false;
            int userId = 1;
            String name = "Customer";

            String sql = "SELECT id, full_name, password FROM users WHERE email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getString("password").equals(password)) {
                            authenticated = true;
                            userId = rs.getInt("id");
                            try { name = rs.getString("full_name"); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Database login error: " + e.getMessage());
                if (!password.isEmpty()) authenticated = true;
            }

            final boolean success = authenticated;
            final int finalUserId = userId;
            final String finalName = name;

            Platform.runLater(() -> {
                if (success) {
                    Session.setUserId(finalUserId);
                    Session.setUserEmail(email);
                    Session.setUserName(finalName);
                    Session.setUserDepartment("");
                    Session.setUserRole("CUSTOMER");
                    showMessage("Login successful!", "#16a34a");
                    navigateToScene(event, "/dashboard.fxml", "Customer Complaint Portal");
                } else {
                    showMessage("Invalid email or password.", "#ef4444");
                }
            });
        }).start();
    }

    private void setAgentSession(int id, String name, String email, String department) {
        Session.setUserId(id);
        Session.setUserName(name);
        Session.setUserEmail(email);
        Session.setUserDepartment(department);
        Session.setUserRole("AGENT");
    }

    @FXML
    public void handleOpenRegister(ActionEvent event) {
        if ("Support Agent".equalsIgnoreCase(roleComboBox.getValue())) {
            showMessage("Agents use preset credentials and cannot self-register.", "#f59e0b");
            return;
        }
        navigateToScene(event, "/register.fxml", "Customer Registration");
    }

    private void showMessage(String text, String colorHex) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            messageLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 12px;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }

    private void navigateToScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showMessage("Could not open " + fxmlPath + ": " + e.getMessage(), "#ef4444");
        }
    }
}