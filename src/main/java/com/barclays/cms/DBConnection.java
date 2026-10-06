package com.barclays.cms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://complaints-db.czoe06ig4twu.ap-south-1.rds.amazonaws.com:3306/complaints_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "BarclaysCMS#2026";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public static void main(String[] args) {
        System.out.println("Connecting to: " + URL);
        try (Connection conn = getConnection()) {
            System.out.println("Database connection established successfully to complaints_db!");
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
