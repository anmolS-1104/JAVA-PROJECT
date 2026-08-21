package com.complaint.system.util;

import com.complaint.system.dto.ComplaintDTO;
import java.util.List;

public class Session {
    private static int userId = 1;
    private static String userName = "Support Agent";
    private static String userEmail = "";
    private static String userRole = "CUSTOMER";
    private static String userDepartment = "General Support";
    private static List<ComplaintDTO> lastComplaintList;
    private static String lastAnalyticsJson;

    // Getters and Setters
    public static int getUserId() { return userId; }
    public static void setUserId(int id) { userId = id; }

    public static String getUserName() { return userName; }
    public static void setUserName(String name) { userName = name; }

    public static String getUserEmail() { return userEmail; }
    public static void setUserEmail(String email) { userEmail = email; }

    public static String getUserRole() { return userRole; }
    public static void setUserRole(String role) { userRole = role; }

    public static String getUserDepartment() { return userDepartment; }
    public static void setUserDepartment(String department) { userDepartment = department; }

    public static List<ComplaintDTO> getLastComplaintList() { return lastComplaintList; }
    public static void setLastComplaintList(List<ComplaintDTO> list) { lastComplaintList = list; }

    public static String getLastAnalyticsJson() { return lastAnalyticsJson; }
    public static void setLastAnalyticsJson(String json) { lastAnalyticsJson = json; }

    public static void clear() {
        userId = 1;
        userName = "Support Agent";
        userEmail = "";
        userRole = "CUSTOMER";
        userDepartment = "General Support";
        if (lastComplaintList != null) lastComplaintList.clear();
        lastAnalyticsJson = null;
    }
}