package com.ticketing.reservation.dto;

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
public class ReservationResponse {

    private Long reservationId;
    private List<SeatInfo> seats;
    private LocalDateTime expiresAt;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
