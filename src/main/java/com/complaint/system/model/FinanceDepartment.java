package com.complaint.system.model;

public class FinanceDepartment implements Department {

    public String handleComplaint(String complaint) {
        return "Finance team will resolve payment issue";
    }
}
