package com.ticketing.booking.repository;

import com.ticketing.booking.domain.BookingSeat;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BookingSeatRepository implements PanacheRepository<BookingSeat> {

    public List<BookingSeat> findByBookingId(Long bookingId) {
        return list("bookingId", bookingId);
    }

    public List<BookingSeat> findBySeatId(Long seatId) {
        return list("seatId", seatId);
    }
}
