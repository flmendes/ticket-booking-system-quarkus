package com.ticketing.booking.service;

import com.ticketing.booking.domain.Booking;
import com.ticketing.booking.domain.BookingSeat;
import com.ticketing.booking.dto.BookingConfirmRequest;
import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.dto.PaymentResponse;
import com.ticketing.booking.exception.InvalidSeatStateException;
import com.ticketing.booking.exception.PaymentFailedException;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.repository.BookingSeatRepository;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.dto.SeatInfo;
import com.ticketing.event.exception.SeatNotFoundException;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.SeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.exception.InvalidReservationException;
import com.ticketing.reservation.exception.ReservationExpiredException;
import com.ticketing.reservation.exception.ReservationNotFoundException;
import com.ticketing.reservation.repository.ReservationRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BookingService {

    @Inject
    BookingRepository bookingRepository;

    @Inject
    BookingSeatRepository bookingSeatRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    SeatRepository seatRepository;

    @Inject
    EventRepository eventRepository;

    @Inject
    PaymentService paymentService;

    /**
     * Confirm booking after payment
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
