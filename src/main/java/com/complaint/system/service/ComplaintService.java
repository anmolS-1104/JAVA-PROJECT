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
        return dao.submitComplaint(description, filePath, priority, deptName);
    }

    public List<ComplaintDTO> getComplaintsByDepartment(String department) {
        return dao.filterComplaints(department, null, null, null);
    }

    public List<ComplaintDTO> getComplaintsByUserId(int userId) {
        return dao.filterComplaints(null, null, null, null);
    }

    public boolean updateStatus(int id, String status) {
        return dao.updateNotes(id, status);
    }

    public boolean deleteComplaint(int id) {
        return dao.updateNotes(id, null);
    }

    // ✅ NEW from Step 5
    public boolean updateNotes(int id, String notes) {
        return dao.updateNotes(id, notes);
    }

    public List<ComplaintDTO> filterComplaints(String department, String status, String priority, String sortBy) {
        return dao.filterComplaints(department, status, priority, sortBy);
    }
}