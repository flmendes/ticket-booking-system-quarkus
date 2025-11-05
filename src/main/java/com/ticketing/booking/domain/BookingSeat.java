package com.ticketing.booking.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "booking_seats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_seat", columnNames = { "booking_id", "seat_id" }),
    },
    indexes = { @Index(name = "idx_seat_id", columnList = "seat_id") }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_seat_id")
    private Long bookingSeatId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
