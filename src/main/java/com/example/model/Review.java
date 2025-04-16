package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String bookingId;  // Reference to the booking
    private String studentId;  // Reference to the student who created the review
    private String tutorId;    // Reference to the tutor being reviewed
    private float rating;      // Rating from 1.0 to 5.0
    private String comment;    // Review comment
    private LocalDateTime createdAt;
} 