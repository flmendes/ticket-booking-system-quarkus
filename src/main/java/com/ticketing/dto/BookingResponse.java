package com.ticketing.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private String bookingReference;
    private Long eventId;
    private String eventName;
    private List<SeatInfo> seats;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
