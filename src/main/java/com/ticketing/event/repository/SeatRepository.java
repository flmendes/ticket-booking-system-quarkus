package com.ticketing.event.repository;

import com.ticketing.event.domain.Seat;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SeatRepository implements PanacheRepository<Seat> {

    /**
     * Find seat by event ID and seat number
     */
    public Optional<Seat> findByEventIdAndSeatNumber(Long eventId, String seatNumber) {
        return find("eventId = ?1 and seatNumber = ?2", eventId, seatNumber).firstResultOptional();
    }

    /**
     * Find seat with pessimistic lock
     */
    @Transactional
    public Optional<Seat> findByEventIdAndSeatNumberWithLock(Long eventId, String seatNumber) {
        return find("eventId = ?1 and seatNumber = ?2", eventId, seatNumber)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResultOptional();
    }

    /**
     * Find available seats for an event
     */
    public List<Seat> findAvailableSeats(Long eventId) {
        return list("eventId = ?1 and status = ?2", eventId, Seat.SeatStatus.AVAILABLE);
    }

    /**
     * Update seat status with optimistic locking
     */
    @Transactional
    public int updateSeatStatusWithVersion(
        Long seatId,
        Seat.SeatStatus newStatus,
        Long expectedVersion,
        String userId,
        LocalDateTime reservedUntil
    ) {
        return update(
            "status = ?1, reservedBy = ?2, reservedUntil = ?3, version = version + 1 " +
                "where seatId = ?4 and version = ?5 and status = ?6",
            newStatus,
            userId,
            reservedUntil,
            seatId,
            expectedVersion,
            Seat.SeatStatus.AVAILABLE
        );
    }

    /**
     * Find seats reserved by a user
     */
    public List<Seat> findReservedSeats(String userId) {
        return list("reservedBy = ?1 and status = ?2", userId, Seat.SeatStatus.RESERVED);
    }

    /**
     * Find expired reservations
     */
    public List<Seat> findExpiredReservations(LocalDateTime now) {
        return list("status = ?1 and reservedUntil < ?2", Seat.SeatStatus.RESERVED, now);
    }

    /**
     * Find seats for multiple seat numbers in a specific event
     */
    public List<Seat> findByEventIdAndSeatNumbers(Long eventId, List<String> seatNumbers) {
        return list("eventId = ?1 and seatNumber in ?2", eventId, seatNumbers);
    }
}
