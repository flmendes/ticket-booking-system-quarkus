package com.ticketing.controller;

import com.ticketing.dto.*;
import com.ticketing.entity.Booking;
import com.ticketing.exception.*;
import com.ticketing.repository.BookingRepository;
import com.ticketing.service.TicketBookingService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingController {

    @Inject
    TicketBookingService bookingService;

    @Inject
    BookingRepository bookingRepository;

    /**
     * Reserve seats for an event
     */
    @POST
    @Path("/reserve")
    public Response reserveSeats(@Valid ReservationRequest request) {
        try {
            Log.infof("Reserving seats request: %s", request);
            ReservationResponse response = bookingService.reserveSeats(request);
            return Response.ok(
                ApiResponse.success(response, "Seats reserved successfully")
            ).build();
        } catch (SeatNotFoundException e) {
            Log.error("Seat not found", e);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (SeatNotAvailableException e) {
            Log.warn("Seat not available", e);
            return Response.status(Response.Status.CONFLICT)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (EventNotFoundException e) {
            Log.error("Event not found", e);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            Log.error("Error reserving seats", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Failed to reserve seats: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Confirm booking after payment
     */
    @POST
    @Path("/confirm")
    public Response confirmBooking(@Valid BookingConfirmRequest request) {
        try {
            Log.infof("Confirming booking for reservation: %d", request.getReservationId());
            BookingResponse response = bookingService.confirmBooking(request);
            return Response.ok(
                ApiResponse.success(response, "Booking confirmed successfully")
            ).build();
        } catch (ReservationNotFoundException e) {
            Log.error("Reservation not found", e);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (ReservationExpiredException e) {
            Log.warn("Reservation expired", e);
            return Response.status(Response.Status.GONE)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (InvalidReservationException | InvalidSeatStateException e) {
            Log.warn("Invalid reservation state", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (PaymentFailedException e) {
            Log.error("Payment failed", e);
            return Response.status(Response.Status.PAYMENT_REQUIRED)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            Log.error("Error confirming booking", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Failed to confirm booking: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Get user bookings
     */
    @GET
    @Path("/user/{userId}")
    public Response getUserBookings(@PathParam("userId") String userId) {
        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);

            List<BookingResponse> responses = bookings
                .stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());

            return Response.ok(
                ApiResponse.success(responses, "Bookings retrieved successfully")
            ).build();
        } catch (Exception e) {
            Log.error("Error retrieving user bookings", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Failed to retrieve bookings: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Get booking by reference
     */
    @GET
    @Path("/reference/{bookingReference}")
    public Response getBookingByReference(@PathParam("bookingReference") String bookingReference) {
        try {
            Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

            BookingResponse response = toBookingResponse(booking);
            return Response.ok(ApiResponse.success(response, "Booking found")).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        } catch (Exception e) {
            Log.error("Error retrieving booking", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Failed to retrieve booking: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(ApiResponse.success("OK", "Booking service is healthy")).build();
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
            .bookingId(booking.getBookingId())
            .bookingReference(booking.getBookingReference())
            .eventId(booking.getEventId())
            .totalAmount(booking.getTotalAmount())
            .status(booking.getStatus().name())
            .paymentStatus(booking.getPaymentStatus().name())
            .confirmedAt(booking.getConfirmedAt())
            .createdAt(booking.getCreatedAt())
            .build();
    }
}
