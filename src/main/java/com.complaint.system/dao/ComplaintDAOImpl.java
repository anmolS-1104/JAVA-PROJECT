package com.complaint.system.dao;

import com.complaint.system.util.DBConnection;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

public class ComplaintDAOImpl implements ComplaintDAO {

    // --- EXISTING COMPLAINT LOGIC ---
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
            e.printStackTrace();
            return false;
        }
    }

    // --- 🔹 NEW REGISTRATION LOGIC (Add this now) ---
    public boolean registerUser(String name, String email, String phone, String password) {
        // This matches the columns we created in MySQL Workbench
        String sql = "INSERT INTO users (full_name, email, phone, password, role) VALUES (?, ?, ?, ?, 'CUSTOMER')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);

            int rowsAffected = ps.executeUpdate();
            System.out.println("Registration successful in DB: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("DB Error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public SecurityProperties.User authenticateUser(String email, String password, String role) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User() {
                    @Override
                    public boolean equals(Object another) {
                        return false;
                    }

                    @Override
                    public String toString() {
                        return "";
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public String getName() {
                        return "";
                    }

                    @Override
                    public String getFullName() {
                        return "";
                    }

                    @Override
                    public void setFullName(String s) {

                    }

                    @Override
                    public Iterator<Group> getGroups() {
                        return null;
                    }

                    @Override
                    public String getPassword() {
                        return "";
                    }

                    @Override
                    public void setPassword(String s) {

                    }

                    @Override
                    public Iterator<Role> getRoles() {
                        return null;
                    }

                    @Override
                    public UserDatabase getUserDatabase() {
                        return null;
                    }

                    @Override
                    public String getUsername() {
                        return "";
                    }

                    @Override
                    public void setUsername(String s) {

                    }

                    @Override
                    public void addGroup(Group group) {

                    }

                    @Override
                    public void addRole(Role role) {

                    }

                    @Override
                    public boolean isInGroup(Group group) {
                        return false;
                    }

                    @Override
                    public boolean isInRole(Role role) {
                        return false;
                    }

                    @Override
                    public void removeGroup(Group group) {

                    }

                    @Override
                    public void removeGroups() {

                    }

                    @Override
                    public void removeRole(Role role) {

                    }

                    @Override
                    public void removeRoles() {

                    }
                };
                user.setUsername(String.valueOf(rs.getLong("id")));
                user.setFullName(rs.getString("full_name"));
                user.getRoles();
                return (SecurityProperties.User) user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Login failed
    }


}