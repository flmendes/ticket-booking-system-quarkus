package com.ticketing.repository;

import com.ticketing.entity.Booking;
import com.ticketing.entity.BookingSeat;
import com.ticketing.entity.Reservation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookingSeatRepository implements PanacheRepository<BookingSeat> {

    public List<BookingSeat> findByBookingId(Long bookingId) {
        return list("bookingId", bookingId);
    }

    public List<BookingSeat> findBySeatId(Long seatId) {
        return list("seatId", seatId);
    }
}
