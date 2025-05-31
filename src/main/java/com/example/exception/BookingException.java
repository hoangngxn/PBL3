package com.example.exception;

import lombok.Getter;

@Getter
public class BookingException extends RuntimeException {
    private final ErrorCode errorCode;

    public BookingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        NOT_STUDENT("Only students can create bookings"),
        POST_NOT_FOUND("Post not found"),
        POST_NOT_AVAILABLE("This post is no longer available for booking"),
        POST_FULL("This post has reached its maximum number of students"),
        POST_ENDED("This post has already ended"),
        SCHEDULE_OVERLAP("This schedule overlaps with one of your existing bookings"),
        BOOKING_NOT_FOUND("Booking not found"),
        NOT_TUTOR("Only tutors can update booking status"),
        NOT_BOOKING_OWNER("You can only modify your own bookings"),
        NOT_PENDING_STATUS("Only pending bookings can be deleted");

        @Getter
        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }
    }
} 