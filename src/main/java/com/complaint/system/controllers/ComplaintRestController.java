package com.complaint.system.controllers;

import com.complaint.system.dto.ComplaintDTO;
import com.complaint.system.model.Complaint;
import com.complaint.system.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintRestController {

    private static final List<String> RESTRICTED_PUBLIC_DOMAINS = Arrays.asList(
            "gmail.com",
            "yahoo.com",
            "outlook.com",
            "hotmail.com",
            "rediffmail.com",
            "icloud.com"
    );

    @Autowired
    private ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<?> submitComplaint(@RequestBody ComplaintDTO dto) {
        // Protection 1: Mandatory Client Company Name
        if (dto.getClientCompanyName() == null || dto.getClientCompanyName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Submission rejected: An authorized Client Company Name is required.");
        }

        // Protection 2: Corporate Domain or Client Org ID Verification
        String email = dto.getUserEmail() != null ? dto.getUserEmail().trim().toLowerCase() : "";
        String domain = email.contains("@") ? email.substring(email.indexOf("@") + 1) : "";

        boolean isPublicDomain = RESTRICTED_PUBLIC_DOMAINS.contains(domain);
        boolean hasValidClientOrgCode = dto.getClientOrgCode() != null
                && dto.getClientOrgCode().trim().toUpperCase().startsWith("CLT-");

        if (isPublicDomain && !hasValidClientOrgCode) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Restricted: Incident registration requires an authorized corporate email or a valid Client Organization Code (e.g., CLT-1001).");
        }

        // Map DTO to Model Entity expected by ComplaintService
        Complaint complaint = new Complaint();
        complaint.setUserId(dto.getUserId() > 0 ? dto.getUserId() : 1);
        complaint.setDescription(dto.getDescription());
        complaint.setDepartment(dto.getDepartment());
        complaint.setPriority(dto.getPriority());

        // Attach enterprise verification metadata into the notes field for persistence
        String clientMetadata = "Client: " + dto.getClientCompanyName().trim()
                + (dto.getClientOrgCode() != null ? " [Code: " + dto.getClientOrgCode().trim() + "]" : "");
        complaint.setNotes(clientMetadata);

        boolean isSaved = complaintService.handleNewComplaint(complaint);

        if (isSaved) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Complaint verified and registered for enterprise client: " + dto.getClientCompanyName());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to record complaint in database.");
        }
    }

    @GetMapping
    public ResponseEntity<List<ComplaintDTO>> fetchComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable int id) {
        ComplaintDTO complaint = complaintService.getComplaintById(id);
        if (complaint != null) {
            return ResponseEntity.ok(complaint);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Complaint not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComplaint(@PathVariable int id) {
        boolean deleted = complaintService.deleteComplaint(id);
        if (deleted) {
            return ResponseEntity.ok("Complaint deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Complaint not found or could not be deleted.");
    }
}