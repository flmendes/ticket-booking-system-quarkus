package com.ticketing.reservation.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reservations",
    indexes = {
        @Index(name = "idx_seat_id", columnList = "seat_id"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_user_id", columnList = "user_id"),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ReservationStatus.ACTIVE;
        }
    }

    public enum ReservationStatus {
        ACTIVE,
        CONFIRMED,
        EXPIRED,
        CANCELLED,
    }
}
