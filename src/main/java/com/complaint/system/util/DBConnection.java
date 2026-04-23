package com.complaint.system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // TODO: replace hardcoded credentials with application.properties values
    private static final String URL = "jdbc:mysql://localhost:3306/complaints_db";
    private static final String USER = "root";
    private static final String PASS = "your_password_here";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found. Check Maven dependencies.", e);
        }
    }
}