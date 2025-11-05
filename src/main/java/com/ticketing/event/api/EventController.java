package com.ticketing.event.api;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.dto.EventResponse;
import com.ticketing.event.dto.SeatInfo;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.SeatRepository;
import com.ticketing.shared.dto.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {

    @Inject
    EventRepository eventRepository;

    @Inject
    SeatRepository seatRepository;

    /**
     * Get all events
     */
    @GET
    public Response getAllEvents() {
        List<Event> events = eventRepository.listAll();
        List<EventResponse> responses = events
            .stream()
            .map(this::toEventResponse)
            .collect(Collectors.toList());

        return Response.ok(
            ApiResponse.success(responses, "Events retrieved successfully")
        ).build();
    }

    /**
     * Get event by ID
     */
    @GET
    @Path("/{eventId}")
    public Response getEventById(@PathParam("eventId") Long eventId) {
        Event event = eventRepository
            .findByIdOptional(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));

        EventResponse response = toEventResponse(event);
        return Response.ok(ApiResponse.success(response, "Event found")).build();
    }

    /**
     * Get upcoming events
     */
    @GET
    @Path("/upcoming")
    public Response getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents();
        List<EventResponse> responses = events
            .stream()
            .map(this::toEventResponse)
            .collect(Collectors.toList());

        return Response.ok(
            ApiResponse.success(responses, "Upcoming events retrieved successfully")
        ).build();
    }

    /**
     * Get events on sale
     */
    @GET
    @Path("/on-sale")
    public Response getEventsOnSale() {
        List<Event> events = eventRepository.findEventsOnSale();
        List<EventResponse> responses = events
            .stream()
            .map(this::toEventResponse)
            .collect(Collectors.toList());

        return Response.ok(
            ApiResponse.success(responses, "Events on sale retrieved successfully")
        ).build();
    }

    /**
     * Get available seats for an event
     */
    @GET
    @Path("/{eventId}/seats/available")
    public Response getAvailableSeats(@PathParam("eventId") Long eventId) {
        // Verify event exists
        eventRepository
            .findByIdOptional(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));

        List<Seat> seats = seatRepository.findAvailableSeats(eventId);
        List<SeatInfo> seatInfos = seats
            .stream()
            .map(this::toSeatInfo)
            .collect(Collectors.toList());

        return Response.ok(
            ApiResponse.success(seatInfos, "Available seats retrieved successfully")
        ).build();
    }

    /**
     * Get all seats for an event
     */
    @GET
    @Path("/{eventId}/seats")
    public Response getAllSeats(@PathParam("eventId") Long eventId) {
        // Verify event exists
        eventRepository
            .findByIdOptional(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));

        List<Seat> seats = seatRepository.list("eventId", eventId);
        List<SeatInfo> seatInfos = seats
            .stream()
            .map(this::toSeatInfo)
            .collect(Collectors.toList());

        return Response.ok(
            ApiResponse.success(seatInfos, "Seats retrieved successfully")
        ).build();
    }

    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
            .eventId(event.getEventId())
            .eventName(event.getEventName())
            .eventDate(event.getEventDate())
            .venueName(event.getVenueName())
            .totalSeats(event.getTotalSeats())
            .availableSeats(event.getAvailableSeats())
            .status(event.getStatus().name())
            .saleStartTime(event.getSaleStartTime())
            .build();
    }

    private SeatInfo toSeatInfo(Seat seat) {
        return SeatInfo.builder()
            .seatId(seat.getSeatId())
            .seatNumber(seat.getSeatNumber())
            .section(seat.getSection())
            .rowNumber(seat.getRowNumber())
            .seatType(seat.getSeatType().name())
            .price(seat.getPrice())
            .status(seat.getStatus().name())
            .build();
    }
}
