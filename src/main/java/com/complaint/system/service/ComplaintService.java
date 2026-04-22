package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.icrs.model.Department;

public class ComplaintService {

    private ComplaintDAOImpl dao = new ComplaintDAOImpl();
    private ClassificationEngine engine = new ClassificationEngine();

    // 🔹 FIX: Make sure this name matches what the Controller calls
    public boolean handleNewComplaint(String description, String filePath) {

        // 1. Get Priority from AI Engine
        String priority = engine.getPriority(description);

        // 2. Get Department from AI Engine
        Department dept = engine.classify(description);
        String deptName = dept.getClass().getSimpleName();

        // 3. Save to DBMS (passing all 4 strings)
        return dao.submitComplaint(description, filePath, priority, deptName);
    }
}