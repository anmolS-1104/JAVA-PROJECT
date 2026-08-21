package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;
import com.complaint.system.util.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HistoryController {

    @FXML private TableView<ComplaintDTO> historyTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idCol;
    @FXML private TableColumn<ComplaintDTO, String> descCol;
    @FXML private TableColumn<ComplaintDTO, String> deptCol;
    @FXML private TableColumn<ComplaintDTO, String> priorityCol;
    @FXML private TableColumn<ComplaintDTO, String> statusCol;

    @FXML
    public void initialize() {
        if (idCol != null) idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (descCol != null) descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (deptCol != null) deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        if (priorityCol != null) priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadComplaints();
    }

    private void loadComplaints() {
        new Thread(() -> {
            List<ComplaintDTO> list = new ArrayList<>();
            String sql = "SELECT id, description, department, priority, status FROM complaints ORDER BY id DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    ComplaintDTO dto = new ComplaintDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setDescription(rs.getString("description"));
                    dto.setDepartment(rs.getString("department"));
                    dto.setPriority(rs.getString("priority"));
                    dto.setStatus(rs.getString("status"));
                    list.add(dto);
                }
            } catch (Exception e) {
                System.err.println("History DB Fetch Error: " + e.getMessage());
                list = Session.getLastComplaintList();
            }

            final List<ComplaintDTO> finalList = list;
            Platform.runLater(() -> {
                if (finalList != null && historyTable != null) {
                    historyTable.setItems(FXCollections.observableArrayList(finalList));
                }
            });
        }).start();
    }

    @FXML
    protected void handleDashboard() {
        navigateTo("/dashboard.fxml", "ICRS - Dashboard");
    }

    @FXML
    protected void handleAnalytics() {
        navigateTo("/analytics.fxml", "ICRS Analytics");
    }

    @FXML
    protected void handleLogout() {
        Session.clear();
        navigateTo("/login.fxml", "ICRS - Login");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) historyTable.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}