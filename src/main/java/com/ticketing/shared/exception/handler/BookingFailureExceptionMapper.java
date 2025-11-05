package com.ticketing.shared.exception.handler;

import com.ticketing.booking.exception.BookingFailureException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class BookingFailureExceptionMapper implements ExceptionMapper<BookingFailureException> {

    @Override
    public Response toResponse(BookingFailureException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            URI.create("/problems/booking-failure"),
            "Booking Failure",
            exception.getMessage()
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(problem)
            .build();
    }
}
