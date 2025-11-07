package com.ticketing.reservation.service;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler service for cleaning up expired reservations
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ReservationCleanupService {

    public static final String QUERY = "status = ?1";
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @ConfigProperty(name = "ticketing.scheduler.cleanup.enabled", defaultValue = "true")
    boolean cleanupEnabled;

    /**
     * Cleanup expired reservations every minute
     */
    @Scheduled(every = "{ticketing.scheduler.cleanup.interval}", delay = 10)
    void cleanupExpiredReservations() {
        if (!cleanupEnabled) {
            return;
        }

        Log.debug("Running expired reservations cleanup job");

        try {
            List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(
                LocalDateTime.now()
            );

            Log.infof("Found %d expired reservations to clean up", expiredReservations.size());

            int cleaned = 0;
            for (Reservation reservation : expiredReservations) {
                if (tryReleaseExpiredReservation(reservation)) {
                    cleaned++;
                }
            }

            if (cleaned > 0) {
                Log.infof("Successfully cleaned up %d expired reservations", cleaned);
            }
        } catch (Exception e) {
            Log.errorf(e, "Error during reservation cleanup job");
        }
    }

    /**
     * Try to release an expired reservation
     *
     * @param reservation the reservation to release
     * @return true if the reservation was successfully released, false otherwise
     */
    private boolean tryReleaseExpiredReservation(Reservation reservation) {
        try {
            reservationService.releaseExpiredReservation(reservation);
            return true;
        } catch (Exception e) {
            Log.errorf(
                e,
                "Failed to release expired reservation: %d",
                reservation.getReservationId()
            );
            return false;
        }
    }

    /**
     * Log statistics about reservations (runs every 5 minutes)
     */
    @Scheduled(cron = "0 */5 * * * ?")
    void logReservationStats() {
        try {
            long activeReservations = reservationRepository.count(
                    QUERY,
                Reservation.ReservationStatus.ACTIVE
            );
            long expiredReservations = reservationRepository.count(
                    QUERY,
                Reservation.ReservationStatus.EXPIRED
            );
            long confirmedReservations = reservationRepository.count(
                    QUERY,
                Reservation.ReservationStatus.CONFIRMED
            );

            Log.infof(
                "Reservation Stats - Active: %d, Expired: %d, Confirmed: %d",
                activeReservations,
                expiredReservations,
                confirmedReservations
            );
        } catch (Exception e) {
            Log.errorf(e, "Error logging reservation statistics");
        }
    }
}
