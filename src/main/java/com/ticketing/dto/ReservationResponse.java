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
public class ReservationResponse {

    private Long reservationId;
    private List<SeatInfo> seats;
    private LocalDateTime expiresAt;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
