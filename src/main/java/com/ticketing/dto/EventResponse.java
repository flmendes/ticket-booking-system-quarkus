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
public class EventResponse {

    private Long eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private String venueName;
    private Integer totalSeats;
    private Integer availableSeats;
    private String status;
    private LocalDateTime saleStartTime;
}
