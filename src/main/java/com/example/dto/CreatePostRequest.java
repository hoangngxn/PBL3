package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

    @NotEmpty(message = "At least one schedule is required")
    private List<ScheduleDTO> schedules;

    private boolean visibility;

    @NotBlank(message = "Grade is required")
    private String grade;

    @Min(value = 1, message = "Maximum students must be at least 1")
    private int maxStudent;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    public boolean getVisibility() {
        return visibility;
    }
} 