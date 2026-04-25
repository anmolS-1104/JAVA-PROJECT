package com.icrs.dao;

import com.icrs.model.Complaint;
import java.util.List;

public interface ComplaintDAO {
    void addComplaint(Complaint complaint);
    List<Complaint> getComplaintsByUser(int userId);
    Complaint getComplaintById(int id);
    void deleteComplaint(int id);
}