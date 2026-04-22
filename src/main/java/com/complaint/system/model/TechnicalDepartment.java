package com.complaint.system.model;

public class TechnicalDepartment implements Department {

    public String handleComplaint(String complaint) {
        return "Technical team will resolve system issue";
    }
}