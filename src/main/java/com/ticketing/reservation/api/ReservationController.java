package com.ticketing.reservation.api;

import com.ticketing.reservation.dto.ReservationRequest;
import com.ticketing.reservation.dto.ReservationResponse;
import com.ticketing.reservation.service.ReservationService;
import com.ticketing.shared.dto.ApiResponse;
import io.quarkus.logging.Log;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

@Path("/api/bookings/reserve")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Reserve seats for an event
     */
    @POST
    public Response reserveSeats(@Valid ReservationRequest request) {
        Log.infof("Reserving seats request: %s", request);
        ReservationResponse response = reservationService.reserveSeats(request);
        return Response.ok(
            ApiResponse.success(response, "Seats reserved successfully")
        ).build();
    }
}
