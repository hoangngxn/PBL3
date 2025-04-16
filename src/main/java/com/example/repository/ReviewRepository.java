package com.example.repository;

import com.example.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTutorId(String tutorId);
    Optional<Review> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
} 