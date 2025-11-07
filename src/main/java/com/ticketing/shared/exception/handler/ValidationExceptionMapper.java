package com.ticketing.shared.exception.handler;

import com.ticketing.shared.dto.ProblemDetail;
import io.quarkus.logging.Log;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;


@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Log.warnf("Validation error: %s", exception.getMessage());

        // Collect all validation errors
        Map<String, String> violations = exception.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                    this::getPropertyPath,
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing // Keep first message if duplicate keys
            ));

        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.BAD_REQUEST.getStatusCode(),
            URI.create("/problems/validation-error"),
            "Validation Failed",
            "One or more fields failed validation"
        );

        problem.addExtension("violations", violations);

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(problem)
            .build();
    }

    private String getPropertyPath(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        // Remove method name prefix if present (e.g., "reserveSeats.request.userId" -> "userId")
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : path;
    }
}
