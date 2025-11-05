package com.ticketing.shared.exception.handler;

import com.ticketing.shared.dto.ProblemDetail;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

/**
 * Global exception handler for all unhandled exceptions
 * This is a catch-all mapper with lowest priority
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Override
    public Response toResponse(Exception exception) {
        Log.errorf(exception, "Unhandled exception occurred: %s", exception.getMessage());

        // In production, don't expose internal error details
        boolean isDevelopment = "dev".equals(profile);

        String detail = isDevelopment
            ? exception.getMessage()
            : "An unexpected error occurred. Please try again later.";

        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            URI.create("/problems/internal-error"),
            "Internal Server Error",
            detail
        );

        // Add exception class name in development mode
        if (isDevelopment) {
            problem.addExtension("exception_type", exception.getClass().getName());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(problem)
            .build();
    }
}
