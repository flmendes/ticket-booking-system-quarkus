package com.ticketing.service;

import com.ticketing.dto.*;
import com.ticketing.entity.*;
import com.ticketing.exception.*;
import com.ticketing.repository.*;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TicketBookingService {

    @Inject
    SeatRepository seatRepository;

    @Inject
    BookingRepository bookingRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    BookingSeatRepository bookingSeatRepository;

    @Inject
    EventRepository eventRepository;

    @Inject
    DistributedLockService lockService;

    @Inject
    PaymentService paymentService;

    @ConfigProperty(name = "ticketing.reservation.timeout-minutes", defaultValue = "10")
    int reservationTimeoutMinutes;

    /**
     * Step 1: Reserve seats with distributed lock
     */
    @Transactional
    public ReservationResponse reserveSeats(ReservationRequest request) {
        Log.infof(
            "Reserving seats for event %d: %s",
            request.getEventId(),
            request.getSeatNumbers()
        );

        // Validate event exists
        Event event = eventRepository
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
            .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
     * Step 2: Confirm booking after payment
     */
    @Transactional
    public BookingResponse confirmBooking(BookingConfirmRequest request) {
        Log.infof("Confirming booking for reservation %d", request.getReservationId());

        // Find reservation
        Reservation reservation = reservationRepository
            .findByIdOptional(request.getReservationId())
            .orElseThrow(ReservationNotFoundException::new);

        // Check if reservation expired
        if (LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            throw new ReservationExpiredException(
                "Reservation expired. Please select seats again."
            );
        }

        if (!reservation.getStatus().equals(Reservation.ReservationStatus.ACTIVE)) {
            throw new InvalidReservationException("Reservation is not active");
        }

        // Verify user owns the reservation
        if (!reservation.getUserId().equals(request.getUserId())) {
            throw new InvalidReservationException("Reservation does not belong to this user");
        }

        // Find all seats for this reservation
        List<Reservation> allReservations = reservationRepository.list(
            "userId = ?1 and eventId = ?2 and status = ?3",
            reservation.getUserId(),
            reservation.getEventId(),
            Reservation.ReservationStatus.ACTIVE
        );

        // Get the seat
        Seat seat = seatRepository
            .findByIdOptional(reservation.getSeatId())
            .orElseThrow(SeatNotFoundException::new);

        // Validate seat state
        if (
            !seat.getStatus().equals(Seat.SeatStatus.RESERVED) ||
            !seat.getReservedBy().equals(request.getUserId())
        ) {
            throw new InvalidSeatStateException("Seat state has changed");
        }

        // Validate payment request
        if (!paymentService.validatePaymentRequest(request.getPaymentRequest())) {
            throw new PaymentFailedException("Invalid payment details");
        }

        // Process payment
        PaymentResponse paymentResponse = paymentService.processPayment(
            request.getPaymentRequest()
        );

        if (!paymentResponse.isSuccess()) {
            throw new PaymentFailedException(
                "Payment failed: " + paymentResponse.getErrorMessage()
            );
        }

        // Create booking
        String bookingReference = generateBookingReference();
        Booking booking = Booking.builder()
            .eventId(reservation.getEventId())
            .userId(request.getUserId())
            .totalAmount(seat.getPrice())
            .status(Booking.BookingStatus.CONFIRMED)
            .paymentId(paymentResponse.getPaymentId())
            .paymentStatus(Booking.PaymentStatus.SUCCESS)
            .bookingReference(bookingReference)
            .confirmedAt(LocalDateTime.now())
            .build();
        bookingRepository.persist(booking);

        // Link seat to booking
        BookingSeat bookingSeat = BookingSeat.builder()
            .bookingId(booking.getBookingId())
            .seatId(seat.getSeatId())
            .price(seat.getPrice())
            .build();
        bookingSeatRepository.persist(bookingSeat);

        // Update seat status
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seat.setBookingId(booking.getBookingId());
        seat.setReservedBy(null);
        seat.setReservedUntil(null);
        seatRepository.persist(seat);

        // Update reservation
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservationRepository.persist(reservation);

        // Update event available seats count
        updateEventSeatCount(reservation.getEventId(), -1);

        Log.infof("Booking confirmed: %s", bookingReference);

        // Get event details
        Event event = eventRepository.findById(reservation.getEventId());

        return BookingResponse.builder()
            .bookingId(booking.getBookingId())
            .bookingReference(bookingReference)
            .eventId(booking.getEventId())
            .eventName(event.getEventName())
            .seats(List.of(toSeatInfo(seat)))
            .totalAmount(booking.getTotalAmount())
            .status(booking.getStatus().name())
            .paymentStatus(booking.getPaymentStatus().name())
            .confirmedAt(booking.getConfirmedAt())
            .createdAt(booking.getCreatedAt())
            .build();
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
                    Log.debugf("Updated event %d available seats: %d", Optional.ofNullable(eventId), newCount);
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

    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        return (
            "BK-" +
            System.currentTimeMillis() +
            "-" +
            UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
    }
}
