package com.ticketing.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException() {
        super("Reservation not found");
    }
}
