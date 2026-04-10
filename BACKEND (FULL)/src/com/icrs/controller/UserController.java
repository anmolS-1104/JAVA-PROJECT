package com.icrs.controller;

import com.icrs.model.User;
import com.icrs.service.UserService;

public class UserController {

    private UserService userService = new UserService();

    public void register(String fullName, String email,
                         String password, String phone, String role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(role);

        boolean success = userService.register(user);
        if (success) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed.");
        }
    }

    public User login(String email, String password, String role) {
        User user = userService.login(email, password, role);
        if (user != null) {
            System.out.println("Login successful! Welcome " + user.getFullName());
        } else {
            System.out.println("Invalid email or password.");
        }
        return user;
    }
}