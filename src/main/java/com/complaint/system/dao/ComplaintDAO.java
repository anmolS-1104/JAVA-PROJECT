package com.complaint.system.dao;

public interface ComplaintDAO {
    // Make sure this has exactly 4 String parameters
    boolean submitComplaint(String desc, String path, String priority, String dept);
}