package com.example.service;

import com.example.dto.CreatePostRequest;
import com.example.dto.UpdatePostRequest;
import com.example.model.Booking;
import com.example.model.Post;
import com.example.model.Schedule;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    private List<Schedule> convertScheduleDTOs(List<com.example.dto.ScheduleDTO> dtos) {
        return dtos.stream()
            .map(dto -> {
                Schedule schedule = new Schedule();
                schedule.setWeekday(dto.getWeekday());
                schedule.setStartHour(dto.getStartHour());
                schedule.setEndHour(dto.getEndHour());
                
                // Validate that end hour is after start hour
                if (dto.getEndHour().isBefore(dto.getStartHour())) {
                    throw new RuntimeException("End hour must be after start hour");
                }
                
                return schedule;
            })
            .collect(Collectors.toList());
    }

    private boolean hasScheduleOverlap(List<Schedule> schedules, List<Post> existingPosts, boolean checkInternalOverlaps) {
        // First check for overlaps within the same schedules list
        if (checkInternalOverlaps) {
            for (int i = 0; i < schedules.size(); i++) {
                for (int j = i + 1; j < schedules.size(); j++) {
                    if (schedules.get(i).overlaps(schedules.get(j))) {
                        return true;
                    }
                }
            }
        }

        // For each existing post
        for (Post post : existingPosts) {
            // Skip if the post is not visible (completed or cancelled)
            if (!post.isVisibility()) {
                continue;
            }
            
            // Skip if the course has ended
            if (post.getEndTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            
            // For each schedule in the new post
            for (Schedule newSchedule : schedules) {
                // For each schedule in the existing post
                for (Schedule existingSchedule : post.getSchedules()) {
                    if (newSchedule.overlaps(existingSchedule)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Post createPost(CreatePostRequest request) {
        User currentUser = userService.getCurrentUser();
        if (!"TUTOR".equals(currentUser.getRole())) {
            throw new RuntimeException("Only tutors can create posts");
        }

        // Validate that end time is after start time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        // Validate that start time is in the future
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start time must be in the future");
        }

        // Convert and validate schedules
        List<Schedule> schedules = convertScheduleDTOs(request.getSchedules());

        // Check for schedule overlaps with tutor's existing posts
        List<Post> tutorPosts = postRepository.findByUserId(currentUser.getId());
        if (hasScheduleOverlap(schedules, tutorPosts, true)) {
            throw new RuntimeException("The schedule overlaps with your existing posts or has conflicting schedules within itself");
        }

        Post post = new Post();
        post.setUserId(currentUser.getId());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setSubject(request.getSubject());
        post.setLocation(request.getLocation());
        post.setSchedules(schedules);
        post.setGrade(request.getGrade());
        post.setCreatedAt(LocalDateTime.now());
        post.setVisibility(request.getVisibility());
        post.setApprovedStudent(0); // Initialize with 0 approved students
        post.setMaxStudent(request.getMaxStudent());
        post.setStartTime(request.getStartTime());
        post.setEndTime(request.getEndTime());

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
        if (request.getSchedules() != null) {
            List<Schedule> schedules = convertScheduleDTOs(request.getSchedules());
            
            // Check for schedule overlaps with tutor's other posts
            List<Post> tutorPosts = postRepository.findByUserId(currentUser.getId());
            tutorPosts.remove(post); // Remove current post from the list
            if (hasScheduleOverlap(schedules, tutorPosts, false)) {
                throw new RuntimeException("The schedule overlaps with your existing posts");
            }
            
            post.setSchedules(schedules);
        }
        if (request.getGrade() != null) {
            post.setGrade(request.getGrade());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getMaxStudent() != null) {
            post.setMaxStudent(request.getMaxStudent());
        }
        if (request.getStartTime() != null) {
            post.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            post.setEndTime(request.getEndTime());
        }

        // Validate that end time is after start time if both are being updated
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        } else if (request.getStartTime() != null && post.getEndTime() != null) {
            if (post.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        } else if (request.getEndTime() != null && post.getStartTime() != null) {
            if (request.getEndTime().isBefore(post.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
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

    public void deletePost(String postId) {
        // First check if post exists
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }
        
        postRepository.deleteById(postId);
    }
    
    public Post adminUpdatePost(String postId, UpdatePostRequest request) {
        // Admin can update any post without ownership check
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
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
        if (request.getSchedules() != null) {
            List<Schedule> schedules = convertScheduleDTOs(request.getSchedules());
            
            // Check for schedule overlaps with tutor's other posts
            List<Post> tutorPosts = postRepository.findByUserId(post.getUserId());
            tutorPosts.remove(post); // Remove current post from the list
            if (hasScheduleOverlap(schedules, tutorPosts, false)) {
                throw new RuntimeException("The schedule overlaps with the tutor's existing posts");
            }
            
            post.setSchedules(schedules);
        }
        if (request.getGrade() != null) {
            post.setGrade(request.getGrade());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getMaxStudent() != null) {
            post.setMaxStudent(request.getMaxStudent());
        }
        if (request.getStartTime() != null) {
            post.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            post.setEndTime(request.getEndTime());
        }

        // Validate that end time is after start time if both are being updated
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        } else if (request.getStartTime() != null && post.getEndTime() != null) {
            if (post.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        } else if (request.getEndTime() != null && post.getStartTime() != null) {
            if (request.getEndTime().isBefore(post.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        }
        
        return postRepository.save(post);
    }
} 