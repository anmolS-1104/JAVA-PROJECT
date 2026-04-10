package com.icrs.controller;

import com.icrs.model.Complaint;
import com.icrs.service.ComplaintService;

import java.util.List;

public class ComplaintController {

    private ComplaintService service = new ComplaintService();

    public void addComplaint(String text) {
        Complaint c = new Complaint();
        c.setDescription(text);

        service.createComplaint(c);
        System.out.println("Complaint added!");
    }

    public void viewComplaints() {
        List<Complaint> list = service.getAllComplaints();

        for (Complaint c : list) {
            System.out.println(c.getId() + " " + c.getDescription());
        }
    }
}
