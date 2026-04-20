package com.complaint.system.dao;

import com.complaint.system.util.DBConnection;
import com.complaint.system.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 🔹 Removed 'abstract' so Spring/Java can actually create this object
public class UserDAOImpl implements UserDAO {

    @Override
    public boolean registerUser(User user) {
        // Order matches your MySQL Workbench screenshot perfectly
        String sql = "INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole() != null ? user.getRole() : "CUSTOMER");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Registration SQL Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User loginUser(String email, String password, String role) {
        // 🔹 Use the 'role' parameter instead of hardcoding 'CUSTOMER'
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND role = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, role); // Now it works for AGENTS too!

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Login SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}