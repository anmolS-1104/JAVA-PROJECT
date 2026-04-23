package com.complaint.system.model;

public class LogisticsDepartment implements Department {

    public String handleComplaint(String complaint) {
        return "Logistics team will resolve delivery issue";
    }

    public String getName() {
        return "Logistics";
    }
}