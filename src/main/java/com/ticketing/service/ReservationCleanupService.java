package com.ticketing.service;

import com.ticketing.entity.Reservation;
import com.ticketing.repository.ReservationRepository;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Scheduler service for cleaning up expired reservations
 */
@ApplicationScoped
public class ReservationCleanupService {

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    TicketBookingService bookingService;

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
                try {
                    bookingService.releaseExpiredReservation(reservation);
                    cleaned++;
                } catch (Exception e) {
                    Log.errorf(
                        e,
                        "Failed to release expired reservation: %d",
                        reservation.getReservationId()
                    );
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
     * Log statistics about reservations (runs every 5 minutes)
     */
    @Scheduled(cron = "0 */5 * * * ?")
    void logReservationStats() {
        try {
            long activeReservations = reservationRepository.count(
                "status = ?1",
                Reservation.ReservationStatus.ACTIVE
            );
            long expiredReservations = reservationRepository.count(
                "status = ?1",
                Reservation.ReservationStatus.EXPIRED
            );
            long confirmedReservations = reservationRepository.count(
                "status = ?1",
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
