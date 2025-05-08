package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Check if current user is admin
        User currentUser = userService.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied");
        }
        
        // Return all users
        List<User> users = userService.getAllUsers();
        
        // Remove sensitive information
        users.forEach(user -> user.setPassword(null));
        
        return ResponseEntity.ok(users);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        // Check if current user is admin
        User currentUser = userService.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied");
        }
        
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
} 