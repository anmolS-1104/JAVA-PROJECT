package com.complaint.system;

public class Session {
    private static int userId;
    private static String userName;
    private static String userRole;
    private static String userDepartment;

    public static void saveSession(int id, String name, String role, String department) {
        userId = id;
        userName = name;
        userRole = role;
        userDepartment = department;
    }

    public static void clear() {
        userId = 0; userName = null; userRole = null; userDepartment = null;
    }

    // Getters
    public static int getUserId() { return userId; }
    public static String getUserName() { return userName; }
    public static String getUserRole() { return userRole; }
    public static String getUserDepartment() { return userDepartment; }
}