package com.ticketing.shared.exception.handler;

import com.ticketing.booking.exception.PaymentFailedException;
import com.ticketing.shared.dto.ProblemDetail;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.net.URI;

@Provider
public class PaymentFailedExceptionMapper implements ExceptionMapper<PaymentFailedException> {

    @Override
    public Response toResponse(PaymentFailedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndType(
            Response.Status.PAYMENT_REQUIRED.getStatusCode(),
            URI.create("/problems/payment-failed"),
            "Payment Failed",
            exception.getMessage()
        );

        problem.addExtension("reason", "payment_processing_failed");

        return Response.status(Response.Status.PAYMENT_REQUIRED)
            .entity(problem)
            .build();
    }
}
