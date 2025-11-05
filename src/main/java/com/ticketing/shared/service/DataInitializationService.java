package com.ticketing.shared.service;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.SeatRepository;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Initialize sample data for testing
 */
@ApplicationScoped
public class DataInitializationService {

    @Inject
    EventRepository eventRepository;

    @Inject
    SeatRepository seatRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        Log.info("Initializing sample data...");

        // Only initialize if database is empty
        if (eventRepository.count() > 0) {
            Log.info("Sample data already exists, skipping initialization");
            return;
        }

        try {
            // Create sample events
            Event taylorSwift = createEvent(
                "Taylor Swift - Eras Tour",
                LocalDateTime.now().plusDays(30),
                "MetLife Stadium",
                10000
            );

            Event coldplay = createEvent(
                "Coldplay - Music of the Spheres",
                LocalDateTime.now().plusDays(45),
                "Madison Square Garden",
                5000
            );

            Event edSheeran = createEvent(
                "Ed Sheeran - Mathematics Tour",
                LocalDateTime.now().plusDays(60),
                "Barclays Center",
                8000
            );

            // Create seats for each event
            createSeatsForEvent(taylorSwift, 100); // Create 100 sample seats
            createSeatsForEvent(coldplay, 100);
            createSeatsForEvent(edSheeran, 100);

            Log.info("Sample data initialized successfully");
        } catch (Exception e) {
            Log.error("Error initializing sample data", e);
        }
    }

    private Event createEvent(String name, LocalDateTime eventDate, String venue, int totalSeats) {
        Event event = Event.builder()
            .eventName(name)
            .eventDate(eventDate)
            .venueName(venue)
            .totalSeats(totalSeats)
            .availableSeats(totalSeats)
            .status(Event.EventStatus.ON_SALE)
            .saleStartTime(LocalDateTime.now().minusDays(1))
            .build();

        eventRepository.persist(event);
        Log.infof("Created event: %s (ID: %d)", name, event.getEventId());
        return event;
    }

    private void createSeatsForEvent(Event event, int numSeats) {
        List<Seat> seats = new ArrayList<>();
        String[] sections = { "VIP", "Premium", "Regular" };
        BigDecimal[] prices = {
            new BigDecimal("299.99"),
            new BigDecimal("149.99"),
            new BigDecimal("79.99"),
        };

        for (int i = 1; i <= numSeats; i++) {
            int sectionIndex = i % 3;
            String section = sections[sectionIndex];
            String row = String.valueOf((char) ('A' + (i / 10)));
            String seatNumber = row + (i % 10);

            Seat.SeatType seatType = switch (sectionIndex) {
                case 0 -> Seat.SeatType.VIP;
                case 1 -> Seat.SeatType.PREMIUM;
                default -> Seat.SeatType.REGULAR;
            };

            Seat seat = Seat.builder()
                .eventId(event.getEventId())
                .seatNumber(seatNumber)
                .section(section)
                .rowNumber(row)
                .seatType(seatType)
                .price(prices[sectionIndex])
                .status(Seat.SeatStatus.AVAILABLE)
                .build();

            seats.add(seat);
        }

        seatRepository.persist(seats);
        Log.infof("Created %d seats for event: %s", numSeats, event.getEventName());
    }
}
