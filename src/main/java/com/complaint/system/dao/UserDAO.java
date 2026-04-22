package com.complaint.system.dao;

import com.complaint.system.model.User;

public interface UserDAO {
    // 🔹 Keep only this for registration
    boolean registerUser(User user);

    // 🔹 Keep ONLY this 3-parameter method
    User loginUser(String email, String password, String role);
}