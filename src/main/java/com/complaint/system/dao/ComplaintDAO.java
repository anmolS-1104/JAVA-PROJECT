package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import java.util.List;

public interface ComplaintDAO {
    boolean submitComplaint(String desc, String path, String priority, String dept, int userId);
    List<ComplaintDTO> findByDepartment(String department);
    List<ComplaintDTO> findByUserId(int userId);
    boolean updateStatus(int id, String status);
}