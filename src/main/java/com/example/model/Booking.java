package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String studentId;  // Reference to student's user ID
    private String tutorId;    // Reference to tutor's user ID
    private String postId;     // Reference to the post
    private String subject;
    private List<Schedule> schedules;  // List of schedules for this booking
    private BookingStatus status;
    private LocalDateTime createdAt;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELED,
        COMPLETED
    }
} 