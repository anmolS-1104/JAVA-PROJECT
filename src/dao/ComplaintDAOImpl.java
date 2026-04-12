package com.icrs.dao;

import com.icrs.model.Complaint;
import com.icrs.util.DBConnection;

import java.sql.*;
import java.util.*;

public class ComplaintDAOImpl implements ComplaintDAO {

    public void save(Complaint c) {
        try {
            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO complaints(description, priority, department, status) VALUES(?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, c.getDescription());
            ps.setString(2, c.getPriority());
            ps.setString(3, c.getDepartment());
            ps.setString(4, c.getStatus());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Complaint> getAll() {
        List<Complaint> list = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM complaints");

            while (rs.next()) {
                Complaint c = new Complaint();
                c.setId(rs.getInt("id"));
                c.setDescription(rs.getString("description"));
                c.setPriority(rs.getString("priority"));
                c.setDepartment(rs.getString("department"));
                c.setStatus(rs.getString("status"));

                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
