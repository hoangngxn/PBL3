package com.example.service;

import com.example.dto.CreateReviewRequest;
import com.example.dto.ReviewResponseDTO;
import com.example.model.Booking;
import com.example.model.Post;
import com.example.model.Review;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.PostRepository;
import com.example.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public Review createReview(CreateReviewRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Creating review for booking: {} by student: {}", request.getBookingId(), currentUser.getId());
        
        // Verify the user is a student
        if (!"STUDENT".equals(currentUser.getRole())) {
            throw new RuntimeException("Only students can create reviews");
        }

        // Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify the student is the owner of the booking
        if (!booking.getStudentId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only review your own bookings");
        }

        // Verify the booking is completed
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Only completed bookings can be reviewed");
        }

        // Check if a review already exists for this booking
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new RuntimeException("A review already exists for this booking");
        }

        // Create and save the review
        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setStudentId(currentUser.getId());
        review.setTutorId(booking.getTutorId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * Get reviews for a tutor with post titles included
     */
    public List<ReviewResponseDTO> getReviewsByTutor(String tutorId) {
        log.info("Getting reviews for tutor: {}", tutorId);
        List<Review> reviews = reviewRepository.findByTutorId(tutorId);
        
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get review for a booking with post title included
     */
    public ReviewResponseDTO getReviewByBooking(String bookingId) {
        User currentUser = userService.getCurrentUser();
        log.info("Getting review for booking: {}", bookingId);
        
        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify the user is associated with this booking
        if (!booking.getStudentId().equals(currentUser.getId()) && !booking.getTutorId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not associated with this booking");
        }

        // Find the review
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Review not found for this booking"));
                
        return mapToResponseDTO(review);
    }
    
    /**
     * Maps a Review entity to a ReviewResponseDTO with additional information
     */
    private ReviewResponseDTO mapToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        
        // Copy basic properties
        dto.setId(review.getId());
        dto.setBookingId(review.getBookingId());
        dto.setStudentId(review.getStudentId());
        dto.setTutorId(review.getTutorId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        
        // Get booking to find postId
        Booking booking = bookingRepository.findById(review.getBookingId())
                .orElse(null);
        
        if (booking != null) {
            // Get post to get title
            Post post = postRepository.findById(booking.getPostId())
                    .orElse(null);
            
            if (post != null) {
                dto.setPostTitle(post.getTitle());
            }
        }
        
        // Get student information
        User student = userService.getUserById(review.getStudentId());
        if (student != null) {
            ReviewResponseDTO.UserInfoDTO studentInfo = new ReviewResponseDTO.UserInfoDTO();
            studentInfo.setId(student.getId());
            studentInfo.setUsername(student.getUsername());
            studentInfo.setFullname(student.getFullname());
            studentInfo.setAvatar(student.getAvatar());
            dto.setStudent(studentInfo);
        }
        
        return dto;
    }
} 