package com.complaint.system.service;

import com.complaint.system.model.Department;
import com.complaint.system.model.FinanceDepartment;
import com.complaint.system.model.LogisticsDepartment;
import com.complaint.system.model.TechnicalDepartment;

public class ClassificationEngine {

    // 🔹 Method to classify complaint to department
    public Department classify(String complaintText) {

        complaintText = complaintText.toLowerCase();

        if (complaintText.contains("payment") || complaintText.contains("refund")) {
            return new FinanceDepartment();
        }
        else if (complaintText.contains("delivery") || complaintText.contains("shipping")) {
            return new LogisticsDepartment();
        }
        else {
            return new TechnicalDepartment();
        }
    }

    // 🔹 Method to assign priority (AI simulation)
    public String getPriority(String complaintText) {

        complaintText = complaintText.toLowerCase();

        if (complaintText.contains("urgent") || complaintText.contains("immediately")) {
            return "HIGH";
        }
        else if (complaintText.contains("delay") || complaintText.contains("late")) {
            return "MEDIUM";
        }
        else {
            return "NORMAL";
        }
    }
}