package com.icrs.dao;

import com.icrs.model.User;

public interface UserDAO {
    boolean registerUser(User user);
    boolean registerAgent(User user);
    User loginUser(String email, String password);
    User loginAgent(String email, String password);
}
