package com.ticketing.event.repository;

import com.ticketing.event.domain.Event;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class EventRepository implements PanacheRepository<Event> {

    /**
     * Find events by status
     */
    public List<Event> findByStatus(Event.EventStatus status) {
        return list("status", status);
    }

    /**
     * Find events on sale
     */
    public List<Event> findEventsOnSale() {
        return list(
            "status = ?1 and saleStartTime <= ?2",
            Event.EventStatus.ON_SALE,
            LocalDateTime.now()
        );
    }

    /**
     * Update available seats count with optimistic locking
     */
    @Transactional
    public int updateAvailableSeats(Long eventId, Long expectedVersion, int newAvailableSeats) {
        return update(
            "availableSeats = ?1, version = version + 1 where eventId = ?2 and version = ?3",
            newAvailableSeats,
            eventId,
            expectedVersion
        );
    }

    /**
     * Increment available seats
     */
    @Transactional
    public int incrementAvailableSeats(Long eventId, int delta) {
        return update("availableSeats = availableSeats + ?1 where eventId = ?2", delta, eventId);
    }

    /**
     * Find upcoming events
     */
    public List<Event> findUpcomingEvents() {
        return list(
            "eventDate > ?1 and status != ?2 order by eventDate",
            LocalDateTime.now(),
            Event.EventStatus.CANCELLED
        );
    }
}
