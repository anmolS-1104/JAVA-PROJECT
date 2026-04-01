package com.icrs.service;

import com.icrs.model.Department;
import com.icrs.model.FinanceDepartment;
import com.icrs.model.LogisticsDepartment;
import com.icrs.model.TechnicalDepartment;

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