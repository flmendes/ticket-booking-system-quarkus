package com.ticketing.exception;

public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException() {
        super("Seat was modified by another user. Please try again.");
    }
}
