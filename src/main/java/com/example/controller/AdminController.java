package com.example.controller;

import com.example.dto.UpdatePostRequest;
import com.example.model.Booking;
import com.example.model.Post;
import com.example.model.User;
import com.example.service.BookingService;
import com.example.service.PostService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final PostService postService;
    private final BookingService bookingService;
    
    private void verifyAdmin() {
        User currentUser = userService.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied: Admin privilege required");
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(@RequestParam(required = false) String role) {
        verifyAdmin();
        
        List<User> users = userService.getAllUsers();
        
        // Filter by role if specified
        if (role != null && !role.isEmpty()) {
            users = users.stream()
                    .filter(user -> role.equals(user.getRole()))
                    .collect(Collectors.toList());
        }
        
        // Remove sensitive information
        users.forEach(user -> user.setPassword(null));
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        verifyAdmin();
        
        User user = userService.getUserById(userId);
        
        // Remove sensitive information
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        verifyAdmin();
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        verifyAdmin();
        return ResponseEntity.ok(postService.getAllPosts());
    }
    
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        verifyAdmin();
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> updatePost(
            @PathVariable String postId,
            @RequestBody UpdatePostRequest request) {
        verifyAdmin();
        return ResponseEntity.ok(postService.adminUpdatePost(postId, request));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        verifyAdmin();
        
        // Count users by role
        List<User> users = userService.getAllUsers();
        long tutorCount = users.stream().filter(user -> "TUTOR".equals(user.getRole())).count();
        long studentCount = users.stream().filter(user -> "STUDENT".equals(user.getRole())).count();
        
        // Count posts
        List<Post> posts = postService.getAllPosts();
        long totalPosts = posts.size();
        long activePosts = posts.stream().filter(Post::isVisibility).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("tutorCount", tutorCount);
        stats.put("studentCount", studentCount);
        stats.put("totalPosts", totalPosts);
        stats.put("activePosts", activePosts);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@RequestParam String userId) {
        verifyAdmin();
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }
} 