package com.icrs.controller;

/*import com.icrs.dto.LoginRequest;
import com.icrs.dto.LoginResponse;
import com.icrs.model.Agent;
import com.icrs.model.User;
import com.icrs.service.AgentService;
import com.icrs.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private UserService userService = new UserService();
    private AgentService agentService = new AgentService();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getRole().equals("CUSTOMER")) {
            User user = userService.login(request.getEmail(), request.getPassword());
            if (user != null) {
                LoginResponse response = new LoginResponse(
                        user.getId(),
                        user.getFullName(),
                        "CUSTOMER"
                );
                return ResponseEntity.ok(response);
            }

        } else if (request.getRole().equals("AGENT")) {
            Agent agent = agentService.login(request.getEmail(), request.getPassword());
            if (agent != null) {
                LoginResponse response = new LoginResponse(
                        agent.getId(),
                        agent.getFullName(),
                        "AGENT"
                );
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password.");
    }
}*/