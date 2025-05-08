package com.example.service;

import com.example.dto.CreatePostRequest;
import com.example.dto.UpdatePostRequest;
import com.example.model.Booking;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public Post createPost(CreatePostRequest request) {
        User currentUser = userService.getCurrentUser();
        if (!"TUTOR".equals(currentUser.getRole())) {
            throw new RuntimeException("Only tutors can create posts");
        }

        Post post = new Post();
        post.setUserId(currentUser.getId());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setSubject(request.getSubject());
        post.setLocation(request.getLocation());
        post.setSchedule(request.getSchedule());
        post.setCreatedAt(LocalDateTime.now());
        post.setVisibility(request.getVisibility());
        post.setApprovedStudent(0); // Initialize with 0 approved students
        post.setMaxStudent(request.getMaxStudent());

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> getPostsByTutor(String tutorId) {
        return postRepository.findByUserId(tutorId);
    }

    public Post getPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public Post updatePost(String postId, UpdatePostRequest request) {
        User currentUser = userService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if the current user is the owner of the post
        if (!post.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own posts");
        }

        // Update fields if they are not null
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }
        if (request.getSubject() != null) {
            post.setSubject(request.getSubject());
        }
        if (request.getLocation() != null) {
            post.setLocation(request.getLocation());
        }
        if (request.getSchedule() != null) {
            post.setSchedule(request.getSchedule());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getMaxStudent() != null) {
            post.setMaxStudent(request.getMaxStudent());
        }

        return postRepository.save(post);
    }

    public void updateApprovedStudentCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Count confirmed bookings for this post
        long confirmedCount = bookingRepository.countByPostIdAndStatus(postId, Booking.BookingStatus.CONFIRMED);
        post.setApprovedStudent((int) confirmedCount);
        
        // Update visibility based on approved student count
        if (post.getApprovedStudent() >= post.getMaxStudent()) {
            post.setVisibility(false);
        }
        
        postRepository.save(post);
    }
} 