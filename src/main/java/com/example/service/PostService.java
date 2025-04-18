package com.example.service;

import com.example.dto.CreatePostRequest;
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