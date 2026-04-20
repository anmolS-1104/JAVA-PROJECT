package com.complaint.system.controllers;

import com.complaint.system.model.User;
import com.complaint.system.dto.LoginRequest;
import com.complaint.system.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 🔹 Passing 3 parameters to match your new DAO/Service logic
        User user = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getRole()
        );

        // If user is not null, the DAO already verified the role in the SQL query!
        if (user != null) {
            System.out.println("✅ Login Success: " + user.getFullName());
            return ResponseEntity.ok(user);
        } else {
            System.out.println("❌ Login Failed for: " + loginRequest.getEmail());
            return ResponseEntity.status(401).body("{\"error\":\"Invalid credentials or role mismatch\"}");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Registration successful\"}");
        }
        return ResponseEntity.status(400).body("{\"error\":\"Registration failed in Database\"}");
    }
}