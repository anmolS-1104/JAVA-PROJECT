package com.icrs.service;

import com.icrs.dao.ComplaintDAO;
import com.icrs.dao.ComplaintDAOImpl;
import com.icrs.model.Complaint;
import com.icrs.model.Department;

import java.util.List;

public class ComplaintService {

    private ComplaintDAO dao = new ComplaintDAOImpl();
    private ClassificationEngine engine = new ClassificationEngine();
    private ResolutionManager resolver = new ResolutionManager();

    public void createComplaint(Complaint complaint) {

        dao.save(complaint);

        String priority = engine.getPriority(complaint.getDescription());

        Department dept = engine.classify(complaint.getDescription());

        String result = resolver.resolve(dept, complaint.getDescription());

        System.out.println("Complaint added!");
        System.out.println("Priority: " + priority);
        System.out.println("Resolved by: " + dept.getClass().getSimpleName());
        System.out.println("Message: " + result);

        com.icrs.util.FileLogger.log(
                complaint.getDescription() + " | " + priority + " | " + dept.getClass().getSimpleName()
        );
    }

    public List<Complaint> getAllComplaints() {
        return dao.getAll();
    }
}