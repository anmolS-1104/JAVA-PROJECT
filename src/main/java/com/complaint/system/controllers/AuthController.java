package com.complaint.system.controllers;

import com.complaint.system.dto.LoginRequest;
import com.complaint.system.model.Agent;
import com.complaint.system.model.User;
import com.complaint.system.service.AgentService;
import com.complaint.system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService = new UserService();
    private final AgentService agentService = new AgentService();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email and Password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        String email = request.getEmail().trim();
        String password = request.getPassword().trim();
        String role = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim()
                : "CUSTOMER";

        System.out.println("Processing login attempt for: [" + email + "] with role: [" + role + "]");

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            User user = userService.login(email, password, "CUSTOMER");
            if (user != null) {
                System.out.println("Customer login successful for: " + user.getEmail());
                return ResponseEntity.ok(user);
            }
        } else if ("AGENT".equalsIgnoreCase(role)) {
            Agent agent = agentService.login(email, password);
            if (agent != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", agent.getId());
                response.put("fullName", agent.getFullName());
                response.put("email", email);
                response.put("role", "AGENT");
                response.put("department", agent.getDepartment());
                System.out.println("Agent login successful for: " + email);
                return ResponseEntity.ok(response);
            }
        }

        System.err.println("Authentication failed for user: [" + email + "] with role: [" + role + "]");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}