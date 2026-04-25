package com.icrs.service;

import com.icrs.dao.ComplaintDAOImpl;
import com.icrs.model.Complaint;
import java.util.List;

public class ComplaintService {

    private ComplaintDAOImpl dao = new ComplaintDAOImpl();

    public void submitComplaint(Complaint complaint) {
        dao.addComplaint(complaint);
    }

    public List<Complaint> getUserComplaints(int userId) {
        return dao.getComplaintsByUser(userId);
    }

    public boolean deleteComplaint(int complaintId, int userId) {
        Complaint complaint = dao.getComplaintById(complaintId);
        if (complaint == null) return false;
        if (complaint.getUserId() != userId) return false;
        dao.deleteComplaint(complaintId);
        return true;
    }

}