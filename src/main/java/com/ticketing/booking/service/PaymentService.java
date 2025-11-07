package com.ticketing.booking.service;

import com.ticketing.booking.dto.PaymentRequest;
import com.ticketing.booking.dto.PaymentResponse;
import com.ticketing.booking.exception.PaymentFailedException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock payment service for demonstration
 * In production, this would integrate with actual payment gateways
 * like Stripe, PayPal, etc.
 */
@ApplicationScoped
public class PaymentService {

    /**
     * Process payment
     * This is a mock implementation for demonstration purposes
     */
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        Log.infof("Processing payment: %s", paymentRequest);

        try {
            // Simulate payment processing delay
            Thread.sleep(100);

            // Mock: 95% success rate for demo purposes
            boolean isSuccess = Math.random() < 0.95;

            if (isSuccess) {
                String paymentId =
                    "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                return PaymentResponse.builder()
                    .paymentId(paymentId)
                    .success(true)
                    .status("SUCCESS")
                    .processedAt(LocalDateTime.now())
                    .build();
            } else {
                return PaymentResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .errorMessage("Payment declined by bank")
                    .processedAt(LocalDateTime.now())
                    .build();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentFailedException("Payment processing interrupted", e);
        } catch (Exception e) {
            Log.errorf(e, "Payment processing error");
            throw new PaymentFailedException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Refund payment (compensating transaction)
     */
    public PaymentResponse refundPayment(String paymentId) {
        Log.infof("Refunding payment: %s", paymentId);

        try {
            // Simulate refund processing
            Thread.sleep(50);

            String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            return PaymentResponse.builder()
                .paymentId(refundId)
                .success(true)
                .status("REFUNDED")
                .processedAt(LocalDateTime.now())
                .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentFailedException("Refund processing interrupted", e);
        } catch (Exception e) {
            Log.errorf(e, "Refund processing error");
            throw new PaymentFailedException("Refund failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate payment request
     */
    public boolean validatePaymentRequest(PaymentRequest request) {
        if (request == null) return false;
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) return false;
        return request.getCardNumber() != null && request.getCardNumber().length() >= 13;
    }
}
