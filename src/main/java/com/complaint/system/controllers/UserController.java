package com.complaint.system.controllers;

import com.complaint.system.model.User;
import com.complaint.system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/users", "/api/auth"})
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Email and password are required.\"}");
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("CUSTOMER");
        }

        boolean success = userService.register(user);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"message\":\"User registered successfully.\",\"userId\":" + user.getId() + "}");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Registration failed. Email might already exist in database.\"}");
        }
    }
}