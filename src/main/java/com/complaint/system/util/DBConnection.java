package com.complaint.system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_RDS_URL =
            "jdbc:mysql://complaints-db.czoe06ig4twu.ap-south-1.rds.amazonaws.com:3306/complaints_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");

        String finalUrl;
        if (envUrl != null && envUrl.contains("3306") && envUrl.contains("complaints_db")) {
            finalUrl = envUrl.trim();
        } else {
            finalUrl = DEFAULT_RDS_URL;
        }

        String user = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (user == null || user.trim().isEmpty()) {
            user = "admin";
        }

        String pass = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (pass == null || pass.trim().isEmpty()) {
            pass = "BarclaysCMS#2026"; // Database instance password
        }

        return DriverManager.getConnection(finalUrl, user.trim(), pass.trim());
    }
}