package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.complaint.system.model.Department;

public class ComplaintService {

    private ComplaintDAOImpl dao = new ComplaintDAOImpl();
    private ClassificationEngine engine = new ClassificationEngine();

    public boolean handleNewComplaint(String description, String filePath) {

        String priority = engine.getPriority(description);

        Department dept = engine.classify(description);
        String deptName = dept.getName();

        return dao.submitComplaint(description, filePath, priority, deptName);
    }
}