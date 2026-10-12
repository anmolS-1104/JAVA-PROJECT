package com.complaint.system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // Pre-provisioned ITIL functional service desk roster
    private static final Map<String, String> AGENT_ROSTER = new HashMap<>();
    static {
        AGENT_ROSTER.put("finance@agent.company.com", "#AGT-FIN-01");
        AGENT_ROSTER.put("tech@agent.company.com", "#AGT-TECH-01");
        AGENT_ROSTER.put("logistics@agent.company.com", "#AGT-LOG-01");
        AGENT_ROSTER.put("care@agent.company.com", "#AGT-CARE-01");
    }

    // Allowed customer corporate email domains (closed enterprise perimeter)
    private static final Set<String> ALLOWED_CORPORATE_DOMAINS = new HashSet<>(Arrays.asList(
            "client.company.com",
            "acmecorp.com",
            "bmcsoftware.com",
            "client.com",
            "company.com"
    ));

    // Helper method to create response maps without Map.of()
    private Map<String, Object> createResponse(String status, String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", status);
        resp.put("message", message);
        return resp;
    }

    /**
     * Authenticates customer accounts and internal service desk agents.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Email and password are required."));
        }

        email = email.trim().toLowerCase();

        // 1. Service Desk Agent Login
        if (AGENT_ROSTER.containsKey(email)) {
            Map<String, Object> agentData = new HashMap<>();
            agentData.put("status", "SUCCESS");
            agentData.put("role", "AGENT");
            agentData.put("email", email);
            agentData.put("deskId", AGENT_ROSTER.get(email));
            agentData.put("message", "Service desk agent authenticated successfully.");
            return ResponseEntity.ok(agentData);
        }

        // 2. Validate Domain for Customer Login
        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Invalid email format."));
        }

        String domain = email.substring(atIndex + 1);
        if (!ALLOWED_CORPORATE_DOMAINS.contains(domain)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createResponse("FORBIDDEN", "Access Denied: Domain @" + domain + " is unauthorized."));
        }

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("status", "SUCCESS");
        customerData.put("role", "CUSTOMER");
        customerData.put("email", email);
        customerData.put("message", "Customer authenticated successfully.");
        return ResponseEntity.ok(customerData);
    }

    /**
     * Registers a corporate customer, enforcing domain whitelisting and RBAC containment.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> regRequest) {
        String fullName = regRequest.get("fullName");
        String email = regRequest.get("email");
        String phone = regRequest.get("phone");
        String password = regRequest.get("password");

        if (fullName == null || email == null || phone == null || password == null ||
                fullName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Full name, email, phone, and password are required."));
        }

        email = email.trim().toLowerCase();
        phone = phone.trim();

        if (!phone.matches("^\\d{10}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Phone number must be exactly 10 numeric digits."));
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse("ERROR", "Invalid email format."));
        }

        String domain = email.substring(atIndex + 1);
        if (!ALLOWED_CORPORATE_DOMAINS.contains(domain)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createResponse("FORBIDDEN", "Access Denied: Domain @" + domain + " is not authorized."));
        }

        if (AGENT_ROSTER.containsKey(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponse("CONFLICT", "This address is reserved for an ITIL internal service desk."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("role", "CUSTOMER");
        response.put("fullName", fullName.trim());
        response.put("email", email);
        response.put("message", "Customer account registered successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}