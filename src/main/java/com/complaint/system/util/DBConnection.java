package com.complaint.system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // 🔹 Replace 'icrs_db' with your actual database name in MySQL Workbench
    private static final String URL = "jdbc:mysql://localhost:3306/complaints_db";
    private static final String USER = "root";
    private static final String PASS = "you_password"; // 🔹 Use your MySQL password (common at SIT is 'root' or '1234')

    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found. Check your Maven dependencies.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Connection failed! Check if MySQL service is running.");
            e.printStackTrace();
            return null;
        }
    }
}