package com.icrs.controller;

import com.icrs.model.User;
import com.icrs.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private UserService userService = new UserService();

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // The service (UserService) and DAO (UserDAOImpl) underneath handle the logic
        boolean success = userService.register(user);

        if (success) {
            return ResponseEntity.ok("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User registration failed.");
        }
    }

    // Keeping the following for reference
    public User login(String email, String password) {
        User user = userService.login(email, password);
        if (user != null) {
            System.out.println("Login successful! Welcome " + user.getFullName());
        } else {
            System.out.println("Invalid email or password.");
        }
        return user;
    }
}