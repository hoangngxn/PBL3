package com.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotBlank(message = "Booking ID is required")
    private String bookingId;
    
    @Min(value = 1, message = "Rating must be at least 1.0")
    @Max(value = 5, message = "Rating must be at most 5.0")
    private float rating;
    
    @Size(max = 500, message = "Comment must be less than 500 characters")
    private String comment;
} 