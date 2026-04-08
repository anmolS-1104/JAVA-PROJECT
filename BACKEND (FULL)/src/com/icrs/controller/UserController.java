package com.icrs.controller;

import com.icrs.model.User;
import com.icrs.service.UserService;

public class UserController {

    private UserService userService = new UserService();

    public void register(String fullName, String email,
                         String password, String phone) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);

        boolean success = userService.register(user);
        if (success) {
            System.out.println("User registered successfully!");
        } else {
            System.out.println("User registration failed.");
        }
    }

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