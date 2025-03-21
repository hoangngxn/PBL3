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
        booking.setSchedule(post.getSchedule());
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

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
} 