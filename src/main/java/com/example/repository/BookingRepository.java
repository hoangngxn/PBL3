package com.example.repository;

import com.example.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByStudentId(String studentId);
    List<Booking> findByTutorId(String tutorId);
    List<Booking> findByPostId(String postId);
    long countByPostIdAndStatus(String postId, Booking.BookingStatus status);
    
    // Find bookings where user is either student or tutor
    List<Booking> findByStudentIdOrTutorId(String studentId, String tutorId);
} 