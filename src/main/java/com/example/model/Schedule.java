package com.example.model;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class Schedule {
    private DayOfWeek weekday;     // Monday to Sunday
    private LocalTime startHour;    // Start time of the class
    private LocalTime endHour;      // End time of the class

    public boolean overlaps(Schedule other) {
        if (this.weekday != other.weekday) {
            return false;
        }
        
        return !(this.endHour.isBefore(other.startHour) || 
                this.startHour.isAfter(other.endHour) ||
                this.endHour.equals(other.startHour) ||  // Allow back-to-back schedules
                this.startHour.equals(other.endHour));   // Allow back-to-back schedules
    }
}