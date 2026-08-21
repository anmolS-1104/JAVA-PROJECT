package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;
import com.complaint.system.util.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AgentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label deptBadge;
    @FXML private Label assignedCountLabel;

    // Filters
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> sortFilter;

    // Table
    @FXML private TableView<ComplaintDTO> complaintTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idCol;
    @FXML private TableColumn<ComplaintDTO, String> descCol;
    @FXML private TableColumn<ComplaintDTO, String> statusCol;
    @FXML private TableColumn<ComplaintDTO, String> priorityCol;
    @FXML private TableColumn<ComplaintDTO, String> notesCol;

    // Bottom Action Bar Elements
    @FXML private ComboBox<String> statusUpdateBox;
    @FXML private TextField noteInputField;
    @FXML private Label actionStatusLabel;

    private final ObservableList<ComplaintDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<ComplaintDTO> filteredData;

    @FXML
    public void initialize() {
        String agentName = Session.getUserName() != null ? Session.getUserName() : "Agent";
        String agentDept = Session.getUserDepartment() != null ? Session.getUserDepartment() : "Finance & Payroll";

        if (welcomeLabel != null) welcomeLabel.setText("Welcome, " + agentName);
        if (deptBadge != null) deptBadge.setText("Assigned Department: " + agentDept);

        // Setup Dropdowns
        if (statusFilter != null && statusFilter.getItems().isEmpty()) {
            statusFilter.getItems().addAll("ALL", "OPEN", "IN_PROGRESS", "RESOLVED");
            statusFilter.setValue("ALL");
        }
        if (priorityFilter != null && priorityFilter.getItems().isEmpty()) {
            priorityFilter.getItems().addAll("ALL", "LOW", "MEDIUM", "HIGH");
            priorityFilter.setValue("ALL");
        }
        if (sortFilter != null && sortFilter.getItems().isEmpty()) {
            sortFilter.getItems().addAll("Newest First", "Oldest First", "Priority (High to Low)");
            sortFilter.setValue("Newest First");
        }
        if (statusUpdateBox != null && statusUpdateBox.getItems().isEmpty()) {
            statusUpdateBox.getItems().addAll("OPEN", "IN_PROGRESS", "RESOLVED");
            statusUpdateBox.setValue("IN_PROGRESS");
        }

        // Setup Table Columns (No Title Column)
        if (idCol != null) idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (descCol != null) descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (priorityCol != null) priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        if (notesCol != null) notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Selection Listener for easy note editing
        if (complaintTable != null) {
            complaintTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    if (statusUpdateBox != null) statusUpdateBox.setValue(newVal.getStatus());
                    if (noteInputField != null) noteInputField.setText(newVal.getNotes());
                }
            });
        }

        loadDepartmentComplaints();
    }

    public void loadDepartmentComplaints() {
        String department = Session.getUserDepartment();
        if (department == null || department.isEmpty()) {
            department = "Finance & Payroll";
        }

        final String targetDept = department;

        new Thread(() -> {
            List<ComplaintDTO> list = new ArrayList<>();
            String sql = "SELECT id, user_id, description, category, department, priority, status, notes " +
                    "FROM complaints WHERE department = ? ORDER BY id DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, targetDept);
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
                        dto.setNotes(note != null ? note : "Awaiting Action");
                        list.add(dto);
                    }
                }
            } catch (Exception e) {
                System.err.println("Agent Load Complaints Error: " + e.getMessage());
            }

            Platform.runLater(() -> {
                masterData.setAll(list);
                if (assignedCountLabel != null) {
                    assignedCountLabel.setText(String.valueOf(masterData.size()));
                }
                applyFilterAndSort();
            });
        }).start();
    }

    @FXML
    public void handleApplyFilter() {
        applyFilterAndSort();
    }

    @FXML
    public void handleReset() {
        if (statusFilter != null) statusFilter.setValue("ALL");
        if (priorityFilter != null) priorityFilter.setValue("ALL");
        if (sortFilter != null) sortFilter.setValue("Newest First");
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String selectedStatus = statusFilter != null ? statusFilter.getValue() : "ALL";
        String selectedPriority = priorityFilter != null ? priorityFilter.getValue() : "ALL";
        String selectedSort = sortFilter != null ? sortFilter.getValue() : "Newest First";

        filteredData = new FilteredList<>(masterData, item -> {
            boolean matchesStatus = "ALL".equalsIgnoreCase(selectedStatus) || item.getStatus().equalsIgnoreCase(selectedStatus);
            boolean matchesPriority = "ALL".equalsIgnoreCase(selectedPriority) || item.getPriority().equalsIgnoreCase(selectedPriority);
            return matchesStatus && matchesPriority;
        });

        SortedList<ComplaintDTO> sortedData = new SortedList<>(filteredData);
        if ("Oldest First".equalsIgnoreCase(selectedSort)) {
            sortedData.setComparator(Comparator.comparingInt(ComplaintDTO::getId));
        } else if ("Priority (High to Low)".equalsIgnoreCase(selectedSort)) {
            sortedData.setComparator((a, b) -> {
                int pA = getPriorityWeight(a.getPriority());
                int pB = getPriorityWeight(b.getPriority());
                return Integer.compare(pB, pA);
            });
        } else {
            sortedData.setComparator((a, b) -> Integer.compare(b.getId(), a.getId()));
        }

        if (complaintTable != null) {
            complaintTable.setItems(sortedData);
        }
    }

    private int getPriorityWeight(String p) {
        if (p == null) return 0;
        switch (p.toUpperCase()) {
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    @FXML
    public void handleSaveUpdate() {
        ComplaintDTO selected = complaintTable != null ? complaintTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showActionStatus("Please select a complaint from the table first.", "#ef4444");
            return;
        }

        String newStatus = statusUpdateBox != null ? statusUpdateBox.getValue() : selected.getStatus();
        String newNote = noteInputField != null ? noteInputField.getText().trim() : "";

        new Thread(() -> {
            boolean updated = false;
            String sql = "UPDATE complaints SET status = ?, notes = ? WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newStatus);
                pstmt.setString(2, newNote.isEmpty() ? "Under Review" : newNote);
                pstmt.setInt(3, selected.getId());

                int rows = pstmt.executeUpdate();
                updated = rows > 0;
            } catch (Exception e) {
                System.err.println("DB Update Error: " + e.getMessage());
            }

            final boolean success = updated;
            Platform.runLater(() -> {
                if (success) {
                    showActionStatus("Complaint #" + selected.getId() + " updated successfully!", "#16a34a");
                    loadDepartmentComplaints();
                } else {
                    showActionStatus("Failed to update complaint in database.", "#ef4444");
                }
            });
        }).start();
    }

    @FXML
    public void handleDeleteComplaint() {
        ComplaintDTO selected = complaintTable != null ? complaintTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showActionStatus("Please select a complaint to delete.", "#ef4444");
            return;
        }

        new Thread(() -> {
            boolean deleted = false;
            String sql = "DELETE FROM complaints WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selected.getId());
                int rows = pstmt.executeUpdate();
                deleted = rows > 0;
            } catch (Exception e) {
                System.err.println("DB Delete Error: " + e.getMessage());
            }

            final boolean success = deleted;
            Platform.runLater(() -> {
                if (success) {
                    showActionStatus("Complaint #" + selected.getId() + " removed.", "#16a34a");
                    if (noteInputField != null) noteInputField.clear();
                    loadDepartmentComplaints();
                } else {
                    showActionStatus("Failed to delete complaint.", "#ef4444");
                }
            });
        }).start();
    }

    private void showActionStatus(String msg, String colorHex) {
        if (actionStatusLabel != null) {
            actionStatusLabel.setText(msg);
            actionStatusLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 12px;");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}