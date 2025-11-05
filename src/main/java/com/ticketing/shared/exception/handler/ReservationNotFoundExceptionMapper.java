package com.ticketing.shared.exception.handler;

import com.ticketing.reservation.exception.ReservationNotFoundException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class ReservationNotFoundExceptionMapper implements ExceptionMapper<ReservationNotFoundException> {

    @Override
    public Response toResponse(ReservationNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.NOT_FOUND.getStatusCode(),
            URI.create("/problems/reservation-not-found"),
            "Reservation Not Found",
            exception.getMessage()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .entity(problem)
            .build();
    }
}
