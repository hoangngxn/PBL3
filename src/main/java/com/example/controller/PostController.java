package com.example.controller;

import com.example.dto.CreatePostRequest;
import com.example.model.Post;
import com.example.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<Post>> getPostsByTutor(@PathVariable String tutorId) {
        return ResponseEntity.ok(postService.getPostsByTutor(tutorId));
    }
} 