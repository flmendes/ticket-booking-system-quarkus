package com.ticketing.booking.dto;

import com.ticketing.event.dto.SeatInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
