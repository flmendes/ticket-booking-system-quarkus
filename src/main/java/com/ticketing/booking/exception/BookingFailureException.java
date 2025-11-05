package com.ticketing.booking.exception;

public class BookingFailureException extends RuntimeException {

    public BookingFailureException(String message) {
        super(message);
    }

    public BookingFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
