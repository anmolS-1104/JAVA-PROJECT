package com.icrs.controller;

import com.icrs.model.Complaint;
import com.icrs.service.ComplaintService;
import java.util.List;

public class ComplaintController {
    private ComplaintService service = new ComplaintService();

    public void submitComplaint(Complaint complaint) {
        service.submitComplaint(complaint);
    }

    public List<Complaint> getMyComplaints(int userId) {
        return service.getUserComplaints(userId);
    }

    public String deleteComplaint(int complaintId, int userId) {
        boolean deleted = service.deleteComplaint(complaintId, userId);
        if (deleted) return "Complaint deleted successfully.";
        return "Delete failed: complaint not found or not yours.";
    }
}