package com.complaint.system.service;

import com.complaint.system.dao.UserDAOImpl;
import com.complaint.system.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAOImpl userDAO = new UserDAOImpl();

    public boolean register(User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return false;
        }
        return userDAO.registerUser(user);
    }

    public User login(String email, String password) {
        return login(email, password, "CUSTOMER");
    }

    public User login(String emailOrName, String password, String role) {
        if (emailOrName == null || password == null) {
            return null;
        }

        User user = userDAO.findByEmail(emailOrName.trim());

        // If not found by email, try fallback login method
        if (user == null) {
            return userDAO.loginUser(emailOrName.trim(), password.trim(), role);
        }

        // Validate password and role
        boolean passwordMatches = user.getPassword() != null && user.getPassword().equals(password.trim());
        boolean roleMatches = role == null || role.trim().isEmpty()
                || (user.getRole() != null && user.getRole().trim().equalsIgnoreCase(role.trim()));

        if (passwordMatches && roleMatches) {
            return user;
        }

        return null;
    }
}