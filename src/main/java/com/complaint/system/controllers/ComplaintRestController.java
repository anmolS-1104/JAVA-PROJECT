package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.model.Complaint;
import com.complaint.system.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintRestController {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintRestController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Complaint Service is UP and running.");
    }

    @GetMapping
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints() {
        List<ComplaintDTO> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable int id) {
        ComplaintDTO complaint = complaintService.getComplaintById(id);
        if (complaint != null) {
            return ResponseEntity.ok(complaint);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Complaint not found with ID: " + id);
    }

    @PostMapping
    public ResponseEntity<String> submitComplaint(@RequestBody Complaint complaint) {
        boolean success = complaintService.handleNewComplaint(complaint);
        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Complaint submitted successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to submit complaint.");
    }

    @GetMapping("/department/{dept}")
    public ResponseEntity<List<ComplaintDTO>> getByDepartment(@PathVariable String dept) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByDepartment(dept);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ComplaintDTO>> getByUserId(@PathVariable int userId) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByUserId(userId);
        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable int id, @RequestParam String status) {
        boolean updated = complaintService.updateStatus(id, status);
        if (updated) {
            return ResponseEntity.ok("Status updated successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update status.");
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<String> updateNotes(@PathVariable int id, @RequestParam String notes) {
        boolean updated = complaintService.updateNotes(id, notes);
        if (updated) {
            return ResponseEntity.ok("Notes updated successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update notes.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComplaint(@PathVariable int id) {
        boolean deleted = complaintService.deleteComplaint(id);
        if (deleted) {
            return ResponseEntity.ok("Complaint deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete complaint.");
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ComplaintDTO>> filterComplaints(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String sortBy) {
        List<ComplaintDTO> complaints = complaintService.filterComplaints(department, status, priority, sortBy);
        return ResponseEntity.ok(complaints);
    }
}