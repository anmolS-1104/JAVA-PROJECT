package com.icrs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/complaints_db",
                "root",
                "ANMOl@2006"
        );
    }
}
