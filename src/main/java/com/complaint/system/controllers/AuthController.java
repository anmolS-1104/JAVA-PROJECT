package com.complaint.system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Map<String, String> AGENT_ROSTER = new HashMap<>();
    static {
        AGENT_ROSTER.put("finance@agent.company.com", "#AGT-FIN-01");
        AGENT_ROSTER.put("tech@agent.company.com", "#AGT-TECH-01");
        AGENT_ROSTER.put("logistics@agent.company.com", "#AGT-LOG-01");
        AGENT_ROSTER.put("care@agent.company.com", "#AGT-CARE-01");
    }

    private Map<String, Object> createResponse(String status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Email and password are required."));
        }

        email = email.trim().toLowerCase();

        // 1. Support Desk Agent Login
        if (AGENT_ROSTER.containsKey(email)) {
            Map<String, Object> agentData = new HashMap<>();
            agentData.put("status", "SUCCESS");
            agentData.put("role", "AGENT");
            agentData.put("email", email);
            agentData.put("deskId", AGENT_ROSTER.get(email));
            return ResponseEntity.ok(agentData);
        }

        // 2. Validate format - accepts all personal and corporate email addresses
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Invalid email address format."));
        }

        // 3. Customer Login
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("status", "SUCCESS");
        customerData.put("role", "CUSTOMER");
        customerData.put("email", email);
        return ResponseEntity.ok(customerData);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> regRequest) {
        String fullName = regRequest.get("fullName");
        String email = regRequest.get("email");
        String phone = regRequest.get("phone");
        String password = regRequest.get("password");

        if (fullName == null || email == null || password == null ||
                fullName.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Full name, email, and password are required."));
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Invalid email address format."));
        }

        if (AGENT_ROSTER.containsKey(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponse("CONFLICT", "This address is reserved for service desk agents."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("role", "CUSTOMER");
        response.put("fullName", fullName.trim());
        response.put("email", email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}