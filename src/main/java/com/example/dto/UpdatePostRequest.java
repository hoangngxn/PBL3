package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String subject;
    
    private String location;
    
    private String schedule;
    
    private Boolean visibility;

    @Min(value = 1, message = "Maximum students must be at least 1")
    private Integer maxStudent;
} 