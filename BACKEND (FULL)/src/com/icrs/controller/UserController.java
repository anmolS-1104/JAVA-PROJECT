package com.icrs.controller;

import com.icrs.model.Complaint;
import com.icrs.model.User;
import com.icrs.service.ComplaintService;
import com.icrs.service.UserService;
import java.util.List;

public class UserController {

    private UserService userService = new UserService();
    private ComplaintService complaintService = new ComplaintService();

    public void register(String fullName, String email,
                         String password, String phone, String role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(role);

        boolean success = userService.register(user);
        if (success) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed.");
        }
    }

    public User login(String email, String password, String role) {
        User user = userService.login(email, password, role);
        if (user != null) {
            System.out.println("Login successful! Welcome " + user.getFullName());
        } else {
            System.out.println("Invalid email or password.");
        }
        return user;
    }

    public void submitComplaint(String description, String priority,
                                String department, int userId) {
        Complaint c = new Complaint();
        c.setDescription(description);
        c.setPriority(priority);
        c.setDepartment(department);
        c.setUserId(userId);
        complaintService.submitComplaint(c);
        System.out.println("Complaint submitted successfully.");
    }

    public void viewMyComplaints(int userId) {
        List<Complaint> complaints = complaintService.getUserComplaints(userId);
        if (complaints.isEmpty()) {
            System.out.println("No complaints found.");
        } else {
            System.out.println("Your complaints:");
            for (Complaint c : complaints) {
                System.out.println("[" + c.getId() + "] " + c.getDescription()
                        + " | " + c.getPriority()
                        + " | " + c.getDepartment());
            }
        }
    }

    public void deleteMyComplaint(int complaintId, int userId) {
        boolean deleted = complaintService.deleteComplaint(complaintId, userId);
        if (deleted) {
            System.out.println("Complaint deleted successfully.");
        } else {
            System.out.println("Delete failed: complaint not found or does not belong to you.");
        }
    }

}