package com.complaint.system.service;

import com.complaint.system.dao.ComplaintDAOImpl;
import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.model.Complaint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintDAOImpl complaintDAO = new ComplaintDAOImpl();
    private final ClassificationEngine classificationEngine = new ClassificationEngine();

    public boolean handleNewComplaint(Complaint complaint) {
        if (complaint == null || complaint.getDescription() == null || complaint.getDescription().trim().isEmpty()) {
            return false;
        }

        int userId = complaint.getUserId() > 0 ? complaint.getUserId() : 1;
        String desc = complaint.getDescription();
        String notes = complaint.getNotes() != null ? complaint.getNotes() : "";

        String dept = (complaint.getDepartment() != null && !complaint.getDepartment().trim().isEmpty())
                ? complaint.getDepartment()
                : classificationEngine.classify(desc).getName();

        String priority = (complaint.getPriority() != null && !complaint.getPriority().trim().isEmpty())
                ? complaint.getPriority()
                : classificationEngine.getPriority(desc);

        return complaintDAO.submitComplaintWithNotes(desc, priority, dept, userId, notes);
    }

    public List<ComplaintDTO> getAllComplaints() {
        return complaintDAO.getAllComplaints();
    }

    public ComplaintDTO getComplaintById(int id) {
        List<ComplaintDTO> all = complaintDAO.getAllComplaints();
        return all.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public List<ComplaintDTO> getComplaintsByDepartment(String dept) {
        return complaintDAO.findByDepartment(dept);
    }

    public List<ComplaintDTO> getComplaintsByUserId(int userId) {
        return complaintDAO.findByUserId(userId);
    }

    public boolean updateStatus(int id, String status) {
        return complaintDAO.updateStatus(id, status);
    }

    public boolean updateNotes(int id, String notes) {
        return complaintDAO.updateNotes(id, notes);
    }

    public boolean deleteComplaint(int id) {
        return complaintDAO.deleteComplaint(id);
    }

    public List<ComplaintDTO> filterComplaints(String dept, String status, String priority, String sortBy) {
        return complaintDAO.filterComplaints(dept, status, priority, sortBy);
    }
}