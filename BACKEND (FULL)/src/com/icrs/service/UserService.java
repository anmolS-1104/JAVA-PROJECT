package com.icrs.service;

import com.icrs.dao.UserDAO;
import com.icrs.dao.UserDAOImpl;
import com.icrs.model.User;

public class UserService {

    private UserDAO userDAO = new UserDAOImpl();

    public boolean register(User user) {
        if (user.getRole().equals("agent")) {
            return userDAO.registerAgent(user);
        } else {
            return userDAO.registerUser(user);
        }
    }

    public User login(String email, String password, String role) {
        if (role.equals("agent")) {
            return userDAO.loginAgent(email, password);
        } else {
            return userDAO.loginUser(email, password);
        }
    }
}