package com.complaint.system.dao;

import com.complaint.system.model.Agent;
import com.complaint.system.util.DBConnection;

import java.sql.*;

public class AgentDAOImpl implements AgentDAO {

    @Override
    public boolean registerAgent(Agent agent) {
        String sql = "INSERT INTO agents (full_name, email, password, department) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, agent.getFullName());
            ps.setString(2, agent.getEmail());
            ps.setString(3, agent.getPassword());
            ps.setString(4, agent.getDepartment());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("AgentDAOImpl registerAgent error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Agent loginAgent(String email, String password) {
        String sql = "SELECT * FROM agents WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Agent agent = new Agent();
                    agent.setId(rs.getInt("id"));
                    agent.setFullName(rs.getString("full_name"));
                    agent.setEmail(rs.getString("email"));
                    agent.setDepartment(rs.getString("department"));
                    return agent;
                }
            }

        } catch (Exception e) {
            System.err.println("AgentDAOImpl loginAgent error: " + e.getMessage());
        }

        return null;
    }
}