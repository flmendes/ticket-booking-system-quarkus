package com.ticketing.shared.exception.handler;

import com.ticketing.reservation.exception.InvalidReservationException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class InvalidReservationExceptionMapper implements ExceptionMapper<InvalidReservationException> {

    @Override
    public Response toResponse(InvalidReservationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.BAD_REQUEST.getStatusCode(),
            URI.create("/problems/invalid-reservation"),
            "Invalid Reservation",
            exception.getMessage()
        );

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(problem)
            .build();
    }
}
