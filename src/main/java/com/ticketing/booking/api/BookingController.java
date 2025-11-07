package com.ticketing.booking.api;

import com.ticketing.booking.domain.Booking;
import com.ticketing.booking.dto.BookingConfirmRequest;
import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.service.BookingService;
import com.ticketing.shared.dto.ApiResponse;
import io.quarkus.logging.Log;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

import java.util.List;


@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    /**
     * Confirm booking after payment
     */
    @POST
    @Path("/confirm")
    public Response confirmBooking(@Valid BookingConfirmRequest request) {
        Log.infof("Confirming booking for reservation: %d", request.getReservationId());
        BookingResponse response = bookingService.confirmBooking(request);
        return Response.ok(
            ApiResponse.success(response, "Booking confirmed successfully")
        ).build();
    }

    /**
     * Get user bookings
     */
    @GET
    @Path("/user/{userId}")
    public Response getUserBookings(@PathParam("userId") String userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        List<BookingResponse> responses = bookings
            .stream()
            .map(this::toBookingResponse)
            .toList();

        return Response.ok(
            ApiResponse.success(responses, "Bookings retrieved successfully")
        ).build();
    }

    /**
     * Get booking by reference
     */
    @GET
    @Path("/reference/{bookingReference}")
    public Response getBookingByReference(@PathParam("bookingReference") String bookingReference) {
        Booking booking = bookingRepository
            .findByBookingReference(bookingReference)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

        BookingResponse response = toBookingResponse(booking);
        return Response.ok(ApiResponse.success(response, "Booking found")).build();
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
