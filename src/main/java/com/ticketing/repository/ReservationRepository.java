package com.ticketing.repository;

import com.ticketing.entity.Reservation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> findByUserId(String userId) {
        return list("userId = ?1 and status = ?2", userId, Reservation.ReservationStatus.ACTIVE);
    }

    public List<Reservation> findExpiredReservations(LocalDateTime now) {
        return list("status = ?1 and expiresAt < ?2", Reservation.ReservationStatus.ACTIVE, now);
    }

    public List<Reservation> findBySeatId(Long seatId) {
        return list("seatId", seatId);
    }

    public Optional<Reservation> findActiveReservationBySeat(Long seatId) {
        return find(
            "seatId = ?1 and status = ?2",
            seatId,
            Reservation.ReservationStatus.ACTIVE
        ).firstResultOptional();
    }
}
