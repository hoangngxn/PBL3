package com.example.service;

import com.example.dto.CreateBookingRequest;
import com.example.exception.BookingException;
import com.example.exception.BookingException.ErrorCode;
import com.example.model.Booking;
import com.example.model.Post;
import com.example.model.Schedule;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostService postService;

    private boolean hasScheduleOverlap(List<Schedule> newSchedules, List<Booking> existingBookings) {
        // Only check active bookings (PENDING or CONFIRMED)
        return existingBookings.stream()
            .filter(booking -> 
                booking.getStatus() == Booking.BookingStatus.PENDING || 
                booking.getStatus() == Booking.BookingStatus.CONFIRMED)
            .anyMatch(booking -> {
                // Get the post to check its time period
                Post post = postRepository.findById(booking.getPostId())
                        .orElse(null);
                
                if (post == null || post.getEndTime().isBefore(LocalDateTime.now())) {
                    return false; // Skip if post not found or has ended
                }
                
                // Check if any of the new schedules overlap with any of the existing schedules
                for (Schedule newSchedule : newSchedules) {
                    for (Schedule existingSchedule : booking.getSchedules()) {
                        if (newSchedule.overlaps(existingSchedule)) {
                            return true;
                        }
                    }
                }
                return false;
            });
    }

    public Booking createBooking(CreateBookingRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Creating booking for student: {}", currentUser.getId());
        
        if (!"STUDENT".equals(currentUser.getRole())) {
            throw new BookingException(ErrorCode.NOT_STUDENT);
        }

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BookingException(ErrorCode.POST_NOT_FOUND));

        // Validate that the post is still active and visible
        if (!post.isVisibility()) {
            throw new BookingException(ErrorCode.POST_NOT_AVAILABLE);
        }

        // Validate that the post hasn't reached its maximum students
        if (post.getApprovedStudent() >= post.getMaxStudent()) {
            throw new BookingException(ErrorCode.POST_FULL);
        }

        // Validate that the post hasn't ended
        if (post.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BookingException(ErrorCode.POST_ENDED);
        }

        // Get student's existing bookings
        List<Booking> studentBookings = bookingRepository.findByStudentId(currentUser.getId());

        // Check for schedule overlaps with existing bookings
        if (hasScheduleOverlap(post.getSchedules(), studentBookings)) {
            throw new BookingException(ErrorCode.SCHEDULE_OVERLAP);
        }

        Booking booking = new Booking();
        booking.setStudentId(currentUser.getId());
        booking.setTutorId(post.getUserId());
        booking.setPostId(post.getId());
        booking.setSubject(post.getSubject());
        booking.setSchedules(post.getSchedules()); // Set all schedules from the post
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsForCurrentUser() {
        User currentUser = userService.getCurrentUser();

        List<Booking> bookings;
        if ("STUDENT".equals(currentUser.getRole())) {
            bookings = bookingRepository.findByStudentId(currentUser.getId());
        } else if ("TUTOR".equals(currentUser.getRole())) {
            bookings = bookingRepository.findByTutorId(currentUser.getId());
        } else {
            throw new RuntimeException("Invalid user role");
        }

        return bookings;
    }

    public Booking updateBookingStatus(String bookingId, Booking.BookingStatus status) {
        User currentUser = userService.getCurrentUser();
        log.info("Updating booking status: {} for tutor: {}", bookingId, currentUser.getId());
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));

        if (!"TUTOR".equals(currentUser.getRole()) || !booking.getTutorId().equals(currentUser.getId())) {
            throw new BookingException(ErrorCode.NOT_TUTOR);
        }

        // Get the old status before updating
        Booking.BookingStatus oldStatus = booking.getStatus();
        
        // Update the booking status
        booking.setStatus(status);
        Booking updatedBooking = bookingRepository.save(booking);

        // If status changed to/from CONFIRMED, update the post's approved student count
        if (oldStatus != status && 
            (oldStatus == Booking.BookingStatus.CONFIRMED || status == Booking.BookingStatus.CONFIRMED)) {
            postService.updateApprovedStudentCount(booking.getPostId());
        }

        return updatedBooking;
    }

    /**
     * Get all bookings associated with a specific user (as either student or tutor)
     * This method is for admin use only
     */
    public List<Booking> getBookingsByUserId(String userId) {
        // Find user to ensure it exists
        userService.getUserById(userId);
        
        // Return bookings where the user is either a student or tutor
        return bookingRepository.findByStudentIdOrTutorId(userId, userId);
    }

    public void deleteBooking(String bookingId) {
        User currentUser = userService.getCurrentUser();
        log.info("Deleting booking: {} by student: {}", bookingId, currentUser.getId());
        
        // Verify the user is a student
        if (!"STUDENT".equals(currentUser.getRole())) {
            throw new BookingException(ErrorCode.NOT_STUDENT);
        }

        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(ErrorCode.BOOKING_NOT_FOUND));

        // Verify the student is the owner of the booking
        if (!booking.getStudentId().equals(currentUser.getId())) {
            throw new BookingException(ErrorCode.NOT_BOOKING_OWNER);
        }

        // Verify the booking is in PENDING status
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException(ErrorCode.NOT_PENDING_STATUS);
        }

        // Delete the booking
        bookingRepository.deleteById(bookingId);
    }
} 