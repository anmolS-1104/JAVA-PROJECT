package com.complaint.system.service;

import com.icrs.model.Department;

public class ResolutionManager {

    public String resolve(Department dept, String complaint) {
        return dept.handleComplaint(complaint);
    }
}
