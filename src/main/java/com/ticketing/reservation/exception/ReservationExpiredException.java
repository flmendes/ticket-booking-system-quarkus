package com.ticketing.reservation.exception;

public class ReservationExpiredException extends RuntimeException {

    public ReservationExpiredException(String message) {
        super(message);
    }

    public ReservationExpiredException() {
        super("Reservation has expired. Please select seats again.");
    }
}
