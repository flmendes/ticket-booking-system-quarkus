package com.ticketing.shared.exception.handler;

import com.ticketing.shared.dto.ProblemDetail;
import com.ticketing.shared.exception.ConcurrentModificationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class ConcurrentModificationExceptionMapper implements ExceptionMapper<ConcurrentModificationException> {

    @Override
    public Response toResponse(ConcurrentModificationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.CONFLICT.getStatusCode(),
            URI.create("/problems/concurrent-modification"),
            "Concurrent Modification",
            exception.getMessage()
        );

        problem.addExtension("reason", "optimistic_lock_failure");
        problem.addExtension("retry_recommended", true);

        return Response.status(Response.Status.CONFLICT)
            .entity(problem)
            .build();
    }
}
