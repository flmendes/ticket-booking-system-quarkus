package com.ticketing.event.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_sale_start_time", columnList = "sale_start_time"),
        @Index(name = "idx_status", columnList = "status"),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(name = "sale_start_time")
    private LocalDateTime saleStartTime;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.UPCOMING;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        // Ensure version is never null before update
        if (version == null) {
            version = 0L;
        }
    }

    public enum EventStatus {
        UPCOMING,
        ON_SALE,
        SOLD_OUT,
        CANCELLED,
    }
}
