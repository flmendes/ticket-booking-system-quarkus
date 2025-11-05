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
public class BookingRepository implements PanacheRepository<Booking> {

    public Optional<Booking> findByBookingReference(String bookingReference) {
        return find("bookingReference", bookingReference).firstResultOptional();
    }

    public List<Booking> findByUserId(String userId) {
        return list("userId = ?1 order by createdAt desc", userId);
    }

    public List<Booking> findByEventId(Long eventId) {
        return list("eventId", eventId);
    }
}
