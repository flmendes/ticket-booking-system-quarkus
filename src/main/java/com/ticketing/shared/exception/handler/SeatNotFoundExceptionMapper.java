package com.ticketing.shared.exception.handler;

import com.ticketing.event.exception.SeatNotFoundException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class SeatNotFoundExceptionMapper implements ExceptionMapper<SeatNotFoundException> {

    @Override
    public Response toResponse(SeatNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.NOT_FOUND.getStatusCode(),
            URI.create("/problems/seat-not-found"),
            "Seat Not Found",
            exception.getMessage()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .entity(problem)
            .build();
    }
}
