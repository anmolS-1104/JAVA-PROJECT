package com.icrs.dao;

import com.icrs.model.Complaint;
import java.util.*;

public class ComplaintDAOImpl implements ComplaintDAO {
    // Temporary in-memory store — replace with DB logic if using MySQL
    private List<Complaint> complaints = new ArrayList<>();
    private int counter = 1;

    @Override
    public void addComplaint(Complaint complaint) {
        complaint.setId(counter++);
        complaints.add(complaint);
    }

    @Override
    public List<Complaint> getComplaintsByUser(int userId) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint c : complaints) {
            if (c.getUserId() == userId) result.add(c);
        }
        return result;
    }

    @Override
    public Complaint getComplaintById(int id) {
        for (Complaint c : complaints) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    @Override
    public void deleteComplaint(int id) {
        complaints.removeIf(c -> c.getId() == id);
    }
}