package com.example.service;

import com.example.dto.CreateBookingRequest;
import com.example.model.Booking;
import com.example.model.Post;
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

    public Booking createBooking(CreateBookingRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Creating booking for student: {}", currentUser.getId());
        
        if (!"STUDENT".equals(currentUser.getRole())) {
            throw new RuntimeException("Only students can create bookings");
        }

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Booking booking = new Booking();
        booking.setStudentId(currentUser.getId());
        booking.setTutorId(post.getUserId());
        booking.setPostId(post.getId());
        booking.setSubject(post.getSubject());
        booking.setSchedule(post.getSchedules().get(0));
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
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"TUTOR".equals(currentUser.getRole()) || !booking.getTutorId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the tutor of this booking can update its status");
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
            throw new RuntimeException("Only students can delete bookings");
        }

        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify the student is the owner of the booking
        if (!booking.getStudentId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own bookings");
        }

        // Verify the booking is in PENDING status
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be deleted");
        }

        // Delete the booking
        bookingRepository.deleteById(bookingId);
    }
} 