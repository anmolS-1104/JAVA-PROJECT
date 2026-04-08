package com.icrs.service;

import com.icrs.dao.UserDAO;
import com.icrs.dao.UserDAOImpl;
import com.icrs.model.User;

public class UserService {

    private UserDAO userDAO = new UserDAOImpl();

    public boolean register(User user) {
        return userDAO.registerUser(user);
    }

    public User login(String email, String password) {
        return userDAO.loginUser(email, password);
    }
}