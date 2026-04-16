package com.complaint.system.dao;
import com.icrs.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ComplaintDAOImpl {

    // IMPORTANT: This must take 3 Strings to match your Controller
    public boolean submitComplaint(String description, String attachmentPath, String status) {
        String sql = "INSERT INTO complaints (description, attachment_path, status) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, description);
            ps.setString(2, attachmentPath);
            ps.setString(3, status);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}