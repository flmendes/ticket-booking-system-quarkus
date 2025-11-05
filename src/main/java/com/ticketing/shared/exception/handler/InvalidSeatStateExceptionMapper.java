package com.ticketing.shared.exception.handler;

import com.ticketing.booking.exception.InvalidSeatStateException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class InvalidSeatStateExceptionMapper implements ExceptionMapper<InvalidSeatStateException> {

    @Override
    public Response toResponse(InvalidSeatStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.CONFLICT.getStatusCode(),
            URI.create("/problems/invalid-seat-state"),
            "Invalid Seat State",
            exception.getMessage()
        );

        problem.addExtension("reason", "state_conflict");

        return Response.status(Response.Status.CONFLICT)
            .entity(problem)
            .build();
    }
}
