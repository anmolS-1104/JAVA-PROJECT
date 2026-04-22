package com.complaint.system.service;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.dao.UserDAOImpl;
import com.complaint.system.model.User;

public class UserService {

    // 🔹 Standard initialization: Points to your actual DAO implementation
    private final UserDAO userDAO = new UserDAOImpl();

    public boolean register(User user) {
        // This now calls the real registerUser in UserDAOImpl
        return userDAO.registerUser(user);
    }

    public User login(String email, String password, String role) {
        // This now calls the real loginUser in UserDAOImpl
        return userDAO.loginUser(email, password, role);
    }
}