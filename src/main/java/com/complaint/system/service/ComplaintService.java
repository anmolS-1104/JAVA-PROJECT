package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.model.Department;

import java.util.List;

public class ComplaintService {

    private ComplaintDAOImpl dao = new ComplaintDAOImpl();
    private ClassificationEngine engine = new ClassificationEngine();

    public boolean handleNewComplaint(int userId, String description, String filePath) {
        String priority = engine.getPriority(description);
        Department dept = engine.classify(description);
        return dao.submitComplaint(description, filePath, priority, dept.getName(), userId);
    }


    public List<ComplaintDTO> getComplaintsByDepartment(String department) {
        return dao.filterComplaints(department, null, null, null);
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

    // ✅ NEW from Step 5
    public boolean updateNotes(int id, String notes) {
        return dao.updateNotes(id, notes);
    }

    public List<ComplaintDTO> filterComplaints(String department, String status, String priority, String sortBy) {
        return dao.filterComplaints(department, status, priority, sortBy);
    }
}