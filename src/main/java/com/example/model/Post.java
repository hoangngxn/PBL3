package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
    private String subject;
    private String location;
    private String schedule;
    private String grade;
    private LocalDateTime createdAt;
    private boolean visibility;
    private int approvedStudent;
    private int maxStudent;
} 