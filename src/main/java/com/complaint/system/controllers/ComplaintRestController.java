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

        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Description is required.\"}");
        }

        boolean saved = complaintService.handleNewComplaint(description, filePath);
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
}