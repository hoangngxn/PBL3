package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreatePostRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Schedule is required")
    private String schedule;

    private boolean visibility;

    @Min(value = 1, message = "Maximum students must be at least 1")
    private int maxStudent;

    public boolean getVisibility() {
        return visibility;
    }
} 