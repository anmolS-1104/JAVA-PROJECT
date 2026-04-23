package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintRestController {

    private final ComplaintService complaintService = new ComplaintService();

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        String filePath = body.getOrDefault("filePath", "");
        String userIdStr = body.get("userId");

        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Description is required.\"}");
        }

        int userId = 0;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"Valid userId is required.\"}");
        }

        boolean saved = complaintService.handleNewComplaint(description, filePath, userId);
        if (saved) {
            return ResponseEntity.status(201).body("{\"message\":\"Complaint submitted successfully.\"}");
        } else {
            return ResponseEntity.status(500).body("{\"error\":\"Failed to save complaint.\"}");
        }
    }

    @GetMapping("/department/{dept}")
    public ResponseEntity<?> getByDepartment(@PathVariable String dept) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByDepartment(dept);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable int userId) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByUserId(userId);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/user/{userId}/analytics")
    public ResponseEntity<?> getAnalyticsByUserId(@PathVariable int userId) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByUserId(userId);

        int total = complaints.size();
        long pending = complaints.stream().filter(c -> c.getStatus() == null || c.getStatus().equalsIgnoreCase("pending")).count();
        long inProgress = complaints.stream().filter(c -> "In Progress".equalsIgnoreCase(c.getStatus())).count();
        long resolved = complaints.stream().filter(c -> "Resolved".equalsIgnoreCase(c.getStatus())).count();
        long finance = complaints.stream().filter(c -> "Finance".equalsIgnoreCase(c.getDepartment())).count();
        long logistics = complaints.stream().filter(c -> "Logistics".equalsIgnoreCase(c.getDepartment())).count();
        long technical = complaints.stream().filter(c -> "Technical".equalsIgnoreCase(c.getDepartment())).count();

        Map<String, Object> analytics = Map.of(
                "total", total,
                "pending", pending,
                "inProgress", inProgress,
                "resolved", resolved,
                "finance", finance,
                "logistics", logistics,
                "technical", technical
        );

        return ResponseEntity.ok(analytics);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Status is required.\"}");
        }

        boolean updated = complaintService.updateStatus(id, status);
        if (updated) {
            return ResponseEntity.ok("{\"message\":\"Status updated.\"}");
        } else {
            return ResponseEntity.status(500).body("{\"error\":\"Update failed.\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        boolean deleted = complaintService.deleteComplaint(id);
        if (deleted) {
            return ResponseEntity.ok("{\"message\":\"Complaint deleted.\"}");
        } else {
            return ResponseEntity.status(500).body("{\"error\":\"Delete failed.\"}");
        }
    }
}