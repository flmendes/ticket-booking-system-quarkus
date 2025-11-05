package com.ticketing.event.exception;

public class SeatNotAvailableException extends RuntimeException {

    public SeatNotAvailableException(String message) {
        super(message);
    }

    public SeatNotAvailableException() {
        super("Seat is not available");
    }
}
