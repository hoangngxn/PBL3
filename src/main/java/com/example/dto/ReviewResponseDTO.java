package com.example.dto;

import com.example.model.Review;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Review responses that includes additional information
 * like post title and student data
 */
@Data
public class ReviewResponseDTO {
    private String id;
    private String bookingId;
    private String studentId;
    private String tutorId;
    private float rating;
    private String comment;
    private LocalDateTime createdAt;
    private String postTitle;  // Added field for post title
    
    // Student information
    private UserInfoDTO student;
    
    @Data
    public static class UserInfoDTO {
        private String id;
        private String username;
        private String fullname;  // Added field for fullname
        private String avatar;
    }
} 