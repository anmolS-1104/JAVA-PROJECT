package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.model.Department;

import java.util.List;

public class ComplaintService {

    private ComplaintDAOImpl dao = new ComplaintDAOImpl();
    private ClassificationEngine engine = new ClassificationEngine();

    public boolean handleNewComplaint(String description, String filePath, int userId) {
        String priority = engine.getPriority(description);
        Department dept = engine.classify(description);
        String deptName = dept.getName();
        return dao.submitComplaint(description, filePath, priority, deptName, userId);
    }

    public List<ComplaintDTO> getComplaintsByDepartment(String department) {
        return dao.findByDepartment(department);
    }

    public List<ComplaintDTO> getComplaintsByUserId(int userId) {
        return dao.findByUserId(userId);
    }

    public boolean updateStatus(int id, String status) {
        return dao.updateStatus(id, status);
    }

    public boolean deleteComplaint(int id) {
        return dao.deleteComplaint(id);
    }
}