package com.ticketing.booking.service;

import com.ticketing.booking.dto.PaymentRequest;
import com.ticketing.booking.dto.PaymentResponse;
import io.quarkus.logging.Log;
import io.quarkus.test.Mock;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock Payment Service for testing.
 * Always returns successful payments to make tests deterministic.
 * In production, PaymentService has 95% success rate with randomization.
 */
@Mock
@Alternative
@Priority(1)
@ApplicationScoped
public class MockPaymentService extends PaymentService {

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        Log.infof("Mock: Processing payment: %s", paymentRequest);

        // Always succeed in tests for deterministic behavior
        String paymentId = "PAY-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PaymentResponse.builder()
            .paymentId(paymentId)
            .success(true)
            .status("SUCCESS")
            .processedAt(LocalDateTime.now())
            .build();
    }

    @Override
    public PaymentResponse refundPayment(String paymentId) {
        Log.infof("Mock: Refunding payment: %s", paymentId);

        String refundId = "REF-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PaymentResponse.builder()
            .paymentId(refundId)
            .success(true)
            .status("REFUNDED")
            .processedAt(LocalDateTime.now())
            .build();
    }

    @Override
    public boolean validatePaymentRequest(PaymentRequest request) {
        // Same validation as parent
        if (request == null) return false;
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) return false;
        return request.getCardNumber() != null && request.getCardNumber().length() >= 13;
    }
}
