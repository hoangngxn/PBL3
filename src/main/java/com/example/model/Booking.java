package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String studentId;  // Reference to student's user ID
    private String tutorId;    // Reference to tutor's user ID
    private String postId;     // Reference to the post
    private String subject;
    private String schedule;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELED,
        COMPLETED
    }
} 