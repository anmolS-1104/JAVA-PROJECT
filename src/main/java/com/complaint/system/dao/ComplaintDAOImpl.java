package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAOImpl implements ComplaintDAO {

    @Override
    public boolean submitComplaint(String desc, String path, String priority, String dept, int userId) {
        return submitComplaintWithNotes(desc, priority, dept, userId, "");
    }

    public boolean submitComplaintWithNotes(String desc, String priority, String dept, int userId, String notes) {
        String sql = "INSERT INTO complaints (description, priority, department, status, user_id, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, desc);
            ps.setString(2, priority);
            ps.setString(3, dept);
            ps.setString(4, "Pending");
            ps.setInt(5, userId);
            ps.setString(6, notes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Insert Failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<ComplaintDTO> getAllComplaints() {
        List<ComplaintDTO> list = new ArrayList<>();
        String sql = "SELECT id, user_id, description, department, priority, status, notes FROM complaints ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("SQL Fetch Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateNotes(int id, String notes) {
        String sql = "UPDATE complaints SET notes = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (SQLException e) {
            e.printStackTrace();
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
        String sql = "SELECT id, user_id, description, department, priority, status, notes FROM complaints WHERE user_id = ? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ComplaintDTO> filterComplaints(String department, String status, String priority, String sortBy) {
        List<ComplaintDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, user_id, description, department, priority, status, notes FROM complaints WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (department != null && !department.trim().isEmpty()) {
            sql.append(" AND LOWER(department) = LOWER(?)");
            params.add(department);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND LOWER(status) = LOWER(?)");
            params.add(status);
        }
        if (priority != null && !priority.trim().isEmpty()) {
            sql.append(" AND LOWER(priority) = LOWER(?)");
            params.add(priority);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ComplaintDTO mapRow(ResultSet rs) throws SQLException {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(rs.getInt("id"));
        dto.setUserId(rs.getInt("user_id"));
        dto.setDescription(rs.getString("description"));
        dto.setDepartment(rs.getString("department"));
        dto.setPriority(rs.getString("priority"));
        dto.setStatus(rs.getString("status"));
        dto.setNotes(rs.getString("notes"));
        return dto;
    }
}