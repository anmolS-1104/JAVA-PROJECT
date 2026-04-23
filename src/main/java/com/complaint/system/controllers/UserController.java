package com.complaint.system.controllers;

import com.complaint.system.model.User;
import com.complaint.system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CUSTOMER");
        }

        boolean success = userService.register(user);

        if (success) {
            return ResponseEntity.status(201).body("{\"message\":\"User registered successfully.\"}");
        } else {
            return ResponseEntity.status(500).body("{\"error\":\"Registration failed. Email may already be in use.\"}");
        }
    }
}