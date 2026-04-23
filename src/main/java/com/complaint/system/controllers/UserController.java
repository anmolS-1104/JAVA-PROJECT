package com.complaint.system.controllers;

import com.complaint.system.model.User;
import com.complaint.system.dto.LoginRequest;
import com.complaint.system.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService = new UserService();

}