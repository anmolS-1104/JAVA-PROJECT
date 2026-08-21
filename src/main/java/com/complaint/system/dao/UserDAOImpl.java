package com.complaint.system.dao;

import com.complaint.system.model.User;
import com.complaint.system.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl {

    // Register a new user
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (full_name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Resolve name supporting both getFullName() or getName()
            String nameVal = "";
            try {
                nameVal = user.getFullName() != null ? user.getFullName() : "";
            } catch (NoSuchMethodError | Exception e) {
                nameVal = user.getName() != null ? user.getName() : "";
            }

            ps.setString(1, nameVal);
            ps.setString(2, user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "");
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone() != null ? user.getPhone() : "");
            ps.setString(5, user.getRole() != null ? user.getRole() : "CUSTOMER");

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Database Insert Error in registerUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Find User by Email
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Login Authentication (email & password match)
    // 3-Argument Login: Checks email/username, password, and matching role
    public User loginUser(String emailOrName, String password, String role) {
        if (emailOrName == null || password == null || role == null) {
            return null;
        }

        String sql = "SELECT * FROM users WHERE (LOWER(email) = LOWER(?) OR LOWER(full_name) = LOWER(?)) AND password = ? AND LOWER(role) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emailOrName.trim());
            ps.setString(2, emailOrName.trim());
            ps.setString(3, password.trim());
            ps.setString(4, role.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in 3-argument loginUser: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Find User by ID
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to map MySQL row to User POJO
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));

        // Support both full_name or name column definitions dynamically
        try {
            user.setFullName(rs.getString("full_name"));
        } catch (SQLException | NoSuchMethodError e) {
            try {
                user.setName(rs.getString("full_name"));
            } catch (Exception ignored) {
                user.setName(rs.getString("name"));
            }
        }

        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        return user;
    }
}