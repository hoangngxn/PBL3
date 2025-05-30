package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class ScheduleDTO {
    @NotNull(message = "Weekday is required")
    private DayOfWeek weekday;

    @NotNull(message = "Start hour is required")
    private LocalTime startHour;

    @NotNull(message = "End hour is required")
    private LocalTime endHour;
} 