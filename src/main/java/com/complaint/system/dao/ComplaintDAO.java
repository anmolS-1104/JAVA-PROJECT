package com.complaint.system.dao;

import com.complaint.system.dto.ComplaintDTO;
import java.util.List;

public interface ComplaintDAO {
    boolean submitComplaint(String desc, String path, String priority, String dept);
    List<ComplaintDTO> findByDepartment(String department);
    boolean updateStatus(int id, String status);
}