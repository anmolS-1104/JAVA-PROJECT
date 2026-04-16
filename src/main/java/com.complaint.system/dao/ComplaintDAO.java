package com.complaint.system.dao;

import com.icrs.model.Complaint;
import java.util.List;

public interface ComplaintDAO {
    void save(Complaint c);
    List<Complaint> getAll();
}
