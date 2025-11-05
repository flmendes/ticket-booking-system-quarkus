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
public class SeatInfo {

    private Long seatId;
    private String seatNumber;
    private String section;
    private String rowNumber;
    private String seatType;
    private BigDecimal price;
    private String status;
}
