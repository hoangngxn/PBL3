package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "bookmarks")
public class Bookmark {
    @Id
    private String id;
    private String userId;  // The user who bookmarked the post
    private String postId;  // The post that was bookmarked
    private LocalDateTime createdAt;
} 