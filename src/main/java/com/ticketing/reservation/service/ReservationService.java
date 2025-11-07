package com.ticketing.reservation.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.dto.SeatInfo;
import com.ticketing.event.exception.EventNotFoundException;
import com.ticketing.event.exception.SeatNotAvailableException;
import com.ticketing.event.exception.SeatNotFoundException;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.SeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.dto.ReservationRequest;
import com.ticketing.reservation.dto.ReservationResponse;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.shared.service.DistributedLockService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



@ApplicationScoped
@RequiredArgsConstructor
public class ReservationService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final DistributedLockService lockService;

    @ConfigProperty(name = "ticketing.reservation.timeout-minutes", defaultValue = "10")
    int reservationTimeoutMinutes;

    /**
     * Reserve seats with distributed lock
     */
    @Transactional
    public ReservationResponse reserveSeats(ReservationRequest request) {
        Log.infof(
            "Reserving seats for event %d: %s",
            request.getEventId(),
            request.getSeatNumbers()
        );

        // Validate event exists
        eventRepository
            .findByIdOptional(request.getEventId())
            .orElseThrow(() ->
                new EventNotFoundException("Event not found: " + request.getEventId())
            );

        // Sort seat numbers to prevent deadlock
        List<String> sortedSeats = new ArrayList<>(request.getSeatNumbers());
        Collections.sort(sortedSeats);

        // Build lock keys
        List<String> lockKeys = sortedSeats
            .stream()
            .map(seat -> lockService.buildSeatLockKey(request.getEventId(), seat))
            .toList();

        String lockValue = lockService.generateLockValue();
        List<String> acquiredLocks = Collections.emptyList();

        try {
            // Acquire all locks
            acquiredLocks = lockService.tryLockMultiple(lockKeys, lockValue);

            if (acquiredLocks.isEmpty()) {
                throw new SeatNotAvailableException(
                    "One or more seats are being processed by another user. Please try again."
                );
            }

            // Calculate reservation expiry
            LocalDateTime reservedUntil = LocalDateTime.now().plusMinutes(
                reservationTimeoutMinutes
            );

            List<Seat> reservedSeats = new ArrayList<>();
            List<Reservation> reservations = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            // Process each seat
            for (String seatNumber : sortedSeats) {
                Seat seat = seatRepository
                    .findByEventIdAndSeatNumber(request.getEventId(), seatNumber)
                    .orElseThrow(() -> new SeatNotFoundException("Seat not found: " + seatNumber));

                if (!seat.getStatus().equals(Seat.SeatStatus.AVAILABLE)) {
                    throw new SeatNotAvailableException(
                        "Seat " + seatNumber + " is no longer available"
                    );
                }

                // Reserve the seat
                seat.setStatus(Seat.SeatStatus.RESERVED);
                seat.setReservedBy(request.getUserId());
                seat.setReservedUntil(reservedUntil);
                seatRepository.persist(seat);
                reservedSeats.add(seat);

                totalAmount = totalAmount.add(seat.getPrice());

                // Create reservation record
                Reservation reservation = Reservation.builder()
                    .seatId(seat.getSeatId())
                    .eventId(request.getEventId())
                    .userId(request.getUserId())
                    .expiresAt(reservedUntil)
                    .status(Reservation.ReservationStatus.ACTIVE)
                    .build();
                reservationRepository.persist(reservation);
                reservations.add(reservation);
            }

            Log.infof(
                "Successfully reserved %d seats for user %s",
                reservedSeats.size(),
                request.getUserId()
            );

            // Build response
            List<SeatInfo> seatInfos = reservedSeats
                .stream()
                .map(this::toSeatInfo)
                .toList();

            return ReservationResponse.builder()
                .reservationId(reservations.get(0).getReservationId())
                .seats(seatInfos)
                .expiresAt(reservedUntil)
                .totalAmount(totalAmount)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        } finally {
            // Always release locks
            lockService.releaseLocks(acquiredLocks, lockValue);
        }
    }

    /**
     * Release expired reservations
     */
    @Transactional
    public void releaseExpiredReservation(Reservation reservation) {
        String lockKey = lockService.buildSeatLockKey(
            reservation.getEventId(),
            getSeatNumber(reservation.getSeatId())
        );

        String lockValue = lockService.generateLockValue();

        if (lockService.tryLock(lockKey, lockValue)) {
            try {
                Seat seat = seatRepository.findByIdOptional(reservation.getSeatId()).orElse(null);
                if (
                    seat != null &&
                    seat.getStatus().equals(Seat.SeatStatus.RESERVED) &&
                    LocalDateTime.now().isAfter(seat.getReservedUntil())
                ) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    seat.setReservedBy(null);
                    seat.setReservedUntil(null);
                    seatRepository.persist(seat);

                    reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
                    reservationRepository.persist(reservation);

                    updateEventSeatCount(reservation.getEventId(), 1);

                    Log.infof("Released expired reservation for seat %d", seat.getSeatId());
                }
            } finally {
                lockService.releaseLock(lockKey, lockValue);
            }
        }
    }

    /**
     * Update event available seats count with retry
     */
    private void updateEventSeatCount(Long eventId, int delta) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            Event event = eventRepository.findByIdOptional(eventId).orElse(null);
            if (event != null) {
                int newCount = event.getAvailableSeats() + delta;
                int updated = eventRepository.updateAvailableSeats(
                    eventId,
                    event.getVersion(),
                    newCount
                );
                if (updated > 0) {
                    Log.debugf("Updated event %d available seats: %d", String.valueOf(eventId), newCount);
                    break;
                }
            }
        }
    }

    /**
     * Get seat number by seat ID
     */
    private String getSeatNumber(Long seatId) {
        return seatRepository.findByIdOptional(seatId).map(Seat::getSeatNumber).orElse("");
    }

    /**
     * Convert Seat to SeatInfo DTO
     */
    private SeatInfo toSeatInfo(Seat seat) {
        return SeatInfo.builder()
            .seatId(seat.getSeatId())
            .seatNumber(seat.getSeatNumber())
            .section(seat.getSection())
            .rowNumber(seat.getRowNumber())
            .seatType(seat.getSeatType().name())
            .price(seat.getPrice())
            .status(seat.getStatus().name())
            .build();
    }
}
