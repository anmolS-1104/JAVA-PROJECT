package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import java.util.List;

public interface ComplaintDAO {
    boolean submitComplaint(String desc, String path, String priority, String dept);
    boolean updateNotes(int id, String notes);
    boolean updateStatus(int id, String status);
    boolean deleteComplaint(int id);
    List<ComplaintDTO> filterComplaints(String department, String status, String priority, String sortBy);
    List<ComplaintDTO> findByDepartment(String department);
    List<ComplaintDTO> findByUserId(int userId);
}