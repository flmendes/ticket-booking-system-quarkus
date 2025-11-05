package com.ticketing.shared.exception.handler;

import com.ticketing.event.exception.EventNotFoundException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class EventNotFoundExceptionMapper implements ExceptionMapper<EventNotFoundException> {

    @Override
    public Response toResponse(EventNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.NOT_FOUND.getStatusCode(),
            URI.create("/problems/event-not-found"),
            "Event Not Found",
            exception.getMessage()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .entity(problem)
            .build();
    }
}
