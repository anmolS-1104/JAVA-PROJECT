package com.complaint.system.controllers;

import com.complaint.system.dto.LoginRequest;
import com.complaint.system.model.Agent;
import com.complaint.system.model.User;
import com.complaint.system.service.AgentService;
import com.complaint.system.service.UserService;
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
        if ("CUSTOMER".equalsIgnoreCase(request.getRole())) {
            User user = userService.login(request.getEmail(), request.getPassword(), "CUSTOMER");
            if (user != null) {
                return ResponseEntity.ok(user);
            }
        } else if ("AGENT".equalsIgnoreCase(request.getRole())) {
            Agent agent = agentService.login(request.getEmail(), request.getPassword());
            if (agent != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", agent.getId());
                response.put("fullName", agent.getFullName());
                response.put("role", "AGENT");
                response.put("department", agent.getDepartment());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("{\"error\":\"Invalid credentials\"}");
    }
}
