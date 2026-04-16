package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.complaint.system.util.FileLogger;
// Remove the Complaint import if you aren't using the class

public class ComplaintService {

    // Use the Implementation directly
    private ComplaintDAOImpl dao = new ComplaintDAOImpl();

    // If you don't have these classes created yet, comment them out to stop the red errors
    // private ClassificationEngine engine = new ClassificationEngine();
    // private ResolutionManager resolver = new ResolutionManager();

    public void createComplaint(String description, String filePath) {

        // 1. Simple AI Logic (Replacing the Engine for now to make it run)
        String priority = description.toLowerCase().contains("urgent") ? "HIGH" : "NORMAL";
        String dept = "General Support";

        // 2. Save to Database using your direct String method
        boolean isSaved = dao.submitComplaint(description, filePath, priority);

        if (isSaved) {
            System.out.println("✅ Complaint added to DB!");
            System.out.println("Priority: " + priority);

            // 3. Log to file using your utility
            FileLogger.log(description + " | " + priority + " | " + dept);
        } else {
            System.out.println("❌ Failed to save complaint.");
        }
    }
}