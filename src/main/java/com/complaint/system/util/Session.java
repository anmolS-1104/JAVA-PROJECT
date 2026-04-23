package com.complaint.system.util;

import com.complaint.system.dto.ComplaintDTO;
import java.util.List;

public class Session {
    private static int userId;
    private static String userName;
    private static String userRole;
    private static String userDepartment;

    private static List<ComplaintDTO> lastComplaintList;
    private static String lastAnalyticsJson;

    public static void saveSession(int id, String name, String role, String department) {
        userId = id;
        userName = name;
        userRole = role;
        userDepartment = department;
    }

    public static void clear() {
        userId = 0;
        userName = null;
        userRole = null;
        userDepartment = null;
        lastComplaintList = null;
        lastAnalyticsJson = null;
    }

    // Setters
    public static void setLastComplaintList(List<ComplaintDTO> list) { lastComplaintList = list; }
    public static void setLastAnalyticsJson(String json) { lastAnalyticsJson = json; }

    // Getters
    public static int getUserId() { return userId; }
    public static String getUserName() { return userName; }
    public static String getUserRole() { return userRole; }
    public static String getUserDepartment() { return userDepartment; }
    public static List<ComplaintDTO> getLastComplaintList() { return lastComplaintList; }
    public static String getLastAnalyticsJson() { return lastAnalyticsJson; }
}