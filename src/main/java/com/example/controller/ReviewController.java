package com.example.controller;

import com.example.dto.CreateReviewRequest;
import com.example.dto.ReviewResponseDTO;
import com.example.model.Review;
import com.example.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByTutor(@PathVariable String tutorId) {
        return ResponseEntity.ok(reviewService.getReviewsByTutor(tutorId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponseDTO> getReviewByBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(reviewService.getReviewByBooking(bookingId));
    }
} 