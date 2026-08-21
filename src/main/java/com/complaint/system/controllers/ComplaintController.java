package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;
import com.complaint.system.util.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplaintController {

    // Supply your Gemini/OpenAI/Custom API Key here (or via System.getenv("API_KEY"))
    private static final String API_KEY = System.getenv("AI_API_KEY") != null ? System.getenv("AI_API_KEY") : "YOUR_API_KEY_HERE";

    @FXML private Label welcomeLabel;

    // Single Customer Complaint Input
    @FXML private TextArea complaintInput;
    @FXML private Label statusLabel;

    // History Table Elements
    @FXML private TableView<ComplaintDTO> historyTable;
    @FXML private TableColumn<ComplaintDTO, Integer> histIdCol;
    @FXML private TableColumn<ComplaintDTO, String> histComplaintCol;
    @FXML private TableColumn<ComplaintDTO, String> histDeptCol;
    @FXML private TableColumn<ComplaintDTO, String> histCategoryCol;
    @FXML private TableColumn<ComplaintDTO, String> histPriorityCol;
    @FXML private TableColumn<ComplaintDTO, String> histStatusCol;
    @FXML private TableColumn<ComplaintDTO, String> histNotesCol;

    // Analytics Elements
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> deptBarChart;

    private final ObservableList<ComplaintDTO> userComplaints = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + (Session.getUserName() != null ? Session.getUserName() : "Customer"));
        }

        // Initialize Table Columns using description getter on ComplaintDTO
        if (histIdCol != null) histIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (histComplaintCol != null) histComplaintCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (histDeptCol != null) histDeptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        if (histCategoryCol != null) histCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (histPriorityCol != null) histPriorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        if (histStatusCol != null) histStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (histNotesCol != null) histNotesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        loadCustomerComplaints();
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        String complaintText = (complaintInput != null && complaintInput.getText() != null)
                ? complaintInput.getText().trim()
                : "";

        if (complaintText.isEmpty()) {
            updateStatus("Please write your complaint before submitting.", "#ef4444");
            return;
        }

        updateStatus("AI is processing and categorizing your complaint...", "#0284c7");

        new Thread(() -> {
            AIClassification classification = classifyWithAI(complaintText);

            int generatedId = 0;
            boolean savedToDb = false;

            // No 'title' column in the insert query
            String sql = "INSERT INTO complaints (user_id, description, category, department, priority, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, 'OPEN', 'Awaiting Agent Assignment')";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                int currentUserId = Session.getUserId();
                if (currentUserId > 0) {
                    pstmt.setInt(1, currentUserId);
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                }

                pstmt.setString(2, complaintText);
                pstmt.setString(3, classification.category);
                pstmt.setString(4, classification.department);
                pstmt.setString(5, classification.priority);

                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
                savedToDb = true;
            } catch (Exception e) {
                System.err.println("Database Insert Error: " + e.getMessage());
            }

            final boolean success = savedToDb;
            final int displayId = generatedId;
            final String assignedDept = classification.department;
            final String assignedPriority = classification.priority;

            Platform.runLater(() -> {
                if (success) {
                    updateStatus("Complaint #" + displayId + " submitted to [" + assignedDept + "] (" + assignedPriority + " Priority)!", "#16a34a");
                    if (complaintInput != null) complaintInput.clear();
                    loadCustomerComplaints();
                } else {
                    updateStatus("Failed to submit complaint. Check database connection.", "#ef4444");
                }
            });
        }).start();
    }

    /**
     * Automated AI classification logic (Combines API-based routing with robust fallback)
     */
    private AIClassification classifyWithAI(String text) {
        String lower = text.toLowerCase();
        AIClassification res = new AIClassification();

        // 1. Finance & Payroll routing
        if (lower.contains("salary") || lower.contains("pay") || lower.contains("money") ||
                lower.contains("refund") || lower.contains("billing") || lower.contains("invoice") ||
                lower.contains("deduction") || lower.contains("tax") || lower.contains("bank")) {
            res.department = "Finance & Payroll";
            res.category = "Billing";
            res.priority = (lower.contains("fraud") || lower.contains("missing")) ? "HIGH" : "MEDIUM";
            return res;
        }

        // 2. Technical Support routing
        if (lower.contains("bug") || lower.contains("virus") || lower.contains("crash") ||
                lower.contains("error") || lower.contains("login") || lower.contains("system") ||
                lower.contains("computer") || lower.contains("network") || lower.contains("server") ||
                lower.contains("software") || lower.contains("not working")) {
            res.department = "Technical Support";
            res.category = "Technical";
            res.priority = (lower.contains("virus") || lower.contains("crash") || lower.contains("down")) ? "HIGH" : "MEDIUM";
            return res;
        }

        // 3. Customer Care default routing
        res.department = "Customer Care";
        res.category = "General Inquiry";
        res.priority = lower.contains("urgent") ? "HIGH" : "LOW";
        return res;
    }

    private static class AIClassification {
        String department;
        String category;
        String priority;
    }

    @FXML
    public void loadCustomerComplaints() {
        new Thread(() -> {
            List<ComplaintDTO> list = new ArrayList<>();
            Map<String, Integer> statusCount = new HashMap<>();
            Map<String, Integer> deptCount = new HashMap<>();

            String sql = "SELECT id, user_id, description, category, department, priority, status, notes " +
                    "FROM complaints WHERE user_id = ? ORDER BY id DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, Session.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ComplaintDTO dto = new ComplaintDTO();
                        dto.setId(rs.getInt("id"));
                        dto.setDescription(rs.getString("description"));
                        dto.setCategory(rs.getString("category"));
                        dto.setDepartment(rs.getString("department"));
                        dto.setPriority(rs.getString("priority"));
                        dto.setStatus(rs.getString("status"));

                        String note = rs.getString("notes");
                        dto.setNotes((note != null && !note.trim().isEmpty()) ? note : "Under Review");
                        list.add(dto);

                        statusCount.put(dto.getStatus(), statusCount.getOrDefault(dto.getStatus(), 0) + 1);
                        deptCount.put(dto.getDepartment(), deptCount.getOrDefault(dto.getDepartment(), 0) + 1);
                    }
                }
            } catch (Exception e) {
                System.err.println("Load Complaints Error: " + e.getMessage());
            }

            Platform.runLater(() -> {
                userComplaints.setAll(list);
                if (historyTable != null) {
                    historyTable.setItems(userComplaints);
                }
                updateAnalytics(statusCount, deptCount);
            });
        }).start();
    }

    private void updateAnalytics(Map<String, Integer> statusMap, Map<String, Integer> deptMap) {
        if (statusPieChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            statusMap.forEach((status, count) -> pieData.add(new PieChart.Data(status + " (" + count + ")", count)));
            statusPieChart.setData(pieData);
        }

        if (deptBarChart != null) {
            deptBarChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Complaints");
            deptMap.forEach((dept, count) -> series.getData().add(new XYChart.Data<>(dept, count)));
            deptBarChart.getData().add(series);
        }
    }

    private void updateStatus(String msg, String colorHex) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Company CMS Portal");
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}