package com.ticketing.shared.exception.handler;

import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.NOT_FOUND.getStatusCode(),
            URI.create("/problems/not-found"),
            "Resource Not Found",
            exception.getMessage() != null ? exception.getMessage() : "The requested resource was not found"
        );

        return Response.status(Response.Status.NOT_FOUND)
            .entity(problem)
            .build();
    }
}
