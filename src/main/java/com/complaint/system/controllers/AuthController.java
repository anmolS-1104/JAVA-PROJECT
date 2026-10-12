package com.complaint.system.controllers;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserDAO userDAO;

    // Allowed enterprise corporate domains under Zero-Trust policy
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "client.company.com",
            "acmecorp.com",
            "bmcsoftware.com",
            "client.com"
    );

    // ==========================================
    // 1. ENTERPRISE REGISTRATION ENDPOINT
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload) {
        String name = payload.get("fullName");
        if (name == null || name.trim().isEmpty()) {
            name = payload.get("name");
        }

        String email = payload.get("email");
        String password = payload.get("password");
        String phone = payload.get("phone");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "ERROR",
                    "message", "Email and password are required fields."
            ));
        }

        String normalizedEmail = email.trim().toLowerCase();
        int atIndex = normalizedEmail.indexOf("@");

        if (atIndex == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "ERROR",
                    "message", "Invalid email format."
            ));
        }

        String domain = normalizedEmail.substring(atIndex + 1);

        // Security Check 1: Perimeter Domain Whitelist (Blocks @gmail.com)
        if (!ALLOWED_DOMAINS.contains(domain)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "ACCESS_DENIED",
                    "error_code", "ERR_UNAUTHORIZED_DOMAIN",
                    "message", "Registration is restricted to authorized enterprise domains only. Public webmail (@gmail.com) is prohibited."
            ));
        }

        // Security Check 2: Check for existing account
        User existingUser = userDAO.findByEmail(normalizedEmail);
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "ERROR",
                    "message", "An account with this corporate email already exists."
            ));
        }

        // Security Check 3: Force CUSTOMER role (Prevents privilege escalation to SUPPORT_AGENT)
        User newUser = new User();
        newUser.setName(name != null ? name.trim() : "Corporate User");
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(password);
        newUser.setPhone(phone != null ? phone.trim() : "0000000000");
        newUser.setRole("CUSTOMER");

        userDAO.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "SUCCESS",
                "message", "Corporate account registered successfully.",
                "email", newUser.getEmail(),
                "role", newUser.getRole()
        ));
    }

    // ==========================================
    // 2. ENTERPRISE LOGIN ENDPOINT
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "ERROR",
                    "message", "Email and password must be provided."
            ));
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userDAO.findByEmail(normalizedEmail);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "ACCESS_DENIED",
                    "error_code", "ERR_USER_NOT_FOUND",
                    "message", "Invalid corporate credentials."
            ));
        }

        if (!user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "ACCESS_DENIED",
                    "error_code", "ERR_INVALID_PASSWORD",
                    "message", "Invalid corporate credentials."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "phone", user.getPhone()
        ));
    }
}