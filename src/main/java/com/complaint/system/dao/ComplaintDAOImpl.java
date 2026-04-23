package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAOImpl implements ComplaintDAO {

    @Override
    public boolean submitComplaint(String desc, String path, String priority, String dept) {
        String sql = "INSERT INTO complaints (description, attachment_path, priority, department) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, desc);
            ps.setString(2, path);
            ps.setString(3, priority);
            ps.setString(4, dept);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("ComplaintDAOImpl submitComplaint error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<ComplaintDTO> findByDepartment(String department) {
        String sql = "SELECT id, description, department, priority, status FROM complaints WHERE department = ?";
        List<ComplaintDTO> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, department);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComplaintDTO dto = new ComplaintDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setDescription(rs.getString("description"));
                    dto.setDepartment(rs.getString("department"));
                    dto.setPriority(rs.getString("priority"));
                    dto.setStatus(rs.getString("status"));
                    list.add(dto);
                }
            }

        } catch (Exception e) {
            System.err.println("ComplaintDAOImpl findByDepartment error: " + e.getMessage());
        }

        return list;
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
            System.err.println("ComplaintDAOImpl updateStatus error: " + e.getMessage());
            return false;
        }
    }
}