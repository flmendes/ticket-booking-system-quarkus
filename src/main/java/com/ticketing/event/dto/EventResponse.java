package com.ticketing.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
