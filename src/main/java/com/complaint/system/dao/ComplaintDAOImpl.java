package com.complaint.system.dao;

import com.complaint.system.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ComplaintDAOImpl implements ComplaintDAO {

    @Override
    public boolean submitComplaint(String desc, String path, String priority, String dept) {
        String sql = "INSERT INTO complaints (description, file_path, priority, department, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, desc);
            ps.setString(2, path);
            ps.setString(3, priority);
            ps.setString(4, dept);
            ps.setString(5, "Pending"); // Default status for new complaints

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
