package com.complaint.system.dao;

import com.complaint.system.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ComplaintDAOImpl implements ComplaintDAO {

    @Override
    public boolean submitComplaint(String desc, String path, String priority, String dept) {
        // Fixed: was inserting into file_path and status, neither of which exist in the schema
        String sql = "INSERT INTO complaints (description, attachment_path, priority, department) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, desc);
            ps.setString(2, path);
            ps.setString(3, priority);
            ps.setString(4, dept);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("ComplaintDAOImpl error: " + e.getMessage());
            return false;
        }
    }
}