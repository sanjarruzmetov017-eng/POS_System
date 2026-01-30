package com.smartpos.controller;

import com.smartpos.model.User;
import com.smartpos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Simple login check (In production, use Spring Security + JWT)
        return userService.findByUsername(request.username())
                .filter(u -> u.getPassword().equals(request.password()))
                .map(u -> ResponseEntity.ok("Login Successful: Token_Placeholder"))
                .orElse(ResponseEntity.status(401).body("Invalid Credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    // DTO for login
    public record LoginRequest(String username, String password) {
    }
}
