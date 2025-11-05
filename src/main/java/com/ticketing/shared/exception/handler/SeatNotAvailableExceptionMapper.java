package com.ticketing.shared.exception.handler;

import com.ticketing.event.exception.SeatNotAvailableException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class SeatNotAvailableExceptionMapper implements ExceptionMapper<SeatNotAvailableException> {

    @Override
    public Response toResponse(SeatNotAvailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.CONFLICT.getStatusCode(),
            URI.create("/problems/seat-not-available"),
            "Seat Not Available",
            exception.getMessage()
        );

        problem.addExtension("reason", "conflict");

        return Response.status(Response.Status.CONFLICT)
            .entity(problem)
            .build();
    }
}
