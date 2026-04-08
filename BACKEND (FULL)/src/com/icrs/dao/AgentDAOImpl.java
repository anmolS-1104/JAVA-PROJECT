package com.icrs.dao;

import com.icrs.model.Agent;
import com.icrs.util.DBConnection;

import java.sql.*;

public class AgentDAOImpl implements AgentDAO {

    public boolean registerAgent(Agent agent) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO agents (full_name, email, password, department) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, agent.getFullName());
            ps.setString(2, agent.getEmail());
            ps.setString(3, agent.getPassword());
            ps.setString(4, agent.getDepartment());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Agent loginAgent(String email, String password) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM agents WHERE email = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Agent agent = new Agent();
                agent.setId(rs.getInt("id"));
                agent.setFullName(rs.getString("full_name"));
                agent.setEmail(rs.getString("email"));
                agent.setDepartment(rs.getString("department"));
                return agent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}