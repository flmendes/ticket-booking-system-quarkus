package com.ticketing.booking.exception;

public class InvalidSeatStateException extends RuntimeException {

    public InvalidSeatStateException(String message) {
        super(message);
    }
}
