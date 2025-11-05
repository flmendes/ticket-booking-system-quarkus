package com.ticketing.event.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_seat", columnNames = { "event_id", "seat_number" }),
    },
    indexes = {
        @Index(name = "idx_event_status", columnList = "event_id, status"),
        @Index(name = "idx_reserved_until", columnList = "reserved_until"),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "section", length = 50)
    private String section;

    @Column(name = "row_number", length = 10)
    private String rowNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType = SeatType.REGULAR;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Column(name = "reserved_by", length = 50)
    private String reservedBy;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SeatStatus.AVAILABLE;
        }
        if (seatType == null) {
            seatType = SeatType.REGULAR;
        }
        if (version == null) {
            version = 0L;
        }
    }

    public enum SeatStatus {
        AVAILABLE,
        RESERVED,
        BOOKED,
        BLOCKED,
    }

    public enum SeatType {
        REGULAR,
        VIP,
        PREMIUM,
    }
}
