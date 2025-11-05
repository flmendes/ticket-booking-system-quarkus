package com.ticketing.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
