package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAOImpl implements ComplaintDAO {

    @Override
    public boolean submitComplaint(String desc, String path, String priority, String dept, int userId) {
        String sql = "INSERT INTO complaints (description, priority, department, status, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, desc);
            ps.setString(2, priority);
            ps.setString(3, dept);
            ps.setString(4, "Pending");
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateNotes(int id, String notes) {
        String sql = "UPDATE complaints SET notes = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating notes: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE complaints SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteComplaint(int id) {
        String sql = "DELETE FROM complaints WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting complaint: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<ComplaintDTO> findByDepartment(String department) {
        return filterComplaints(department, null, null, null);
    }

    @Override
    public List<ComplaintDTO> findByUserId(int userId) {
        List<ComplaintDTO> list = new ArrayList<>();
        String sql = "SELECT id, description, department, priority, status, notes FROM complaints WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComplaintDTO dto = new ComplaintDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setDescription(rs.getString("description"));
                    dto.setDepartment(rs.getString("department"));
                    dto.setPriority(rs.getString("priority"));
                    dto.setStatus(rs.getString("status"));
                    dto.setNotes(rs.getString("notes"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching by userId: " + e.getMessage());
        }
        return list;
    }
    @Override
    public List<ComplaintDTO> filterComplaints(String department, String status, String priority, String sortBy) {
        List<ComplaintDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, description, department, priority, status, notes FROM complaints WHERE 1=1");

        if (department != null && !department.isEmpty()) sql.append(" AND department = '").append(department).append("'");
        if (status != null && !status.isEmpty()) sql.append(" AND status = '").append(status).append("'");
        if (priority != null && !priority.isEmpty()) sql.append(" AND priority = '").append(priority).append("'");

        if ("date".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY id DESC");
        } else if ("priority".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY FIELD(priority, 'HIGH', 'MEDIUM', 'NORMAL')");
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ComplaintDTO dto = new ComplaintDTO();
                dto.setId(rs.getInt("id"));
                dto.setDescription(rs.getString("description"));
                dto.setDepartment(rs.getString("department"));
                dto.setPriority(rs.getString("priority"));
                dto.setStatus(rs.getString("status"));
                dto.setNotes(rs.getString("notes"));
                list.add(dto);
            }
        } catch (Exception e) {
            System.err.println("Error filtering complaints: " + e.getMessage());
        }
        return list;
    }
}