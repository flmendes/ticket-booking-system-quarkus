package com.ticketing.exception;

public class InvalidSeatStateException extends RuntimeException {

    public InvalidSeatStateException(String message) {
        super(message);
    }
}
