package com.ticketing.shared.exception.handler;

import com.ticketing.reservation.exception.ReservationExpiredException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class ReservationExpiredExceptionMapper implements ExceptionMapper<ReservationExpiredException> {

    @Override
    public Response toResponse(ReservationExpiredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.GONE.getStatusCode(),
            URI.create("/problems/reservation-expired"),
            "Reservation Expired",
            exception.getMessage()
        );

        problem.addExtension("reason", "expired");

        return Response.status(Response.Status.GONE)
            .entity(problem)
            .build();
    }
}
