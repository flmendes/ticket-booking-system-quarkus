package com.ticketing.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling simulation specifically designed to test race conditions
 * Multiple users attempt to book the same seats simultaneously
 */
public class RaceConditionSimulation extends Simulation {

    // Configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int CONCURRENT_USERS = Integer.parseInt(System.getProperty("users", "100"));
    private static final String TARGET_SEAT = System.getProperty("targetSeat", "A1");
    private static final Long EVENT_ID = Long.parseLong(System.getProperty("eventId", "1"));

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Race Condition Test");

    // Counter for unique user IDs
    private static final AtomicInteger userCounter = new AtomicInteger(0);

    // Feeder that generates unique user IDs but targets the same seats
    Iterator<Map<String, Object>> sameSeatsFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
            return Map.of(
                "userId", "race-user-" + userCounter.incrementAndGet(),
                "eventId", EVENT_ID,
                "seatNumber", TARGET_SEAT,
                "cardNumber", "4111111111111111",
                "cvv", "123",
                "expiryDate", "12/2025",
                "cardHolderName", "Race Test User"
            );
        }).iterator();

    // Feeder for multiple overlapping seats
    // Uses actual seats from test data (VIP-1 to VIP-10 from Event 1)
    Iterator<Map<String, Object>> overlappingSeatsFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            // Only 10 different VIP seats, so many users will compete for the same seats
            String seat = "VIP-" + random.nextInt(1, 11);
            return Map.of(
                "userId", "overlap-user-" + userCounter.incrementAndGet(),
                "eventId", 1L, // Use Event 1 which has VIP seats
                "seatNumber", seat,
                "cardNumber", "4111111111111111",
                "cvv", "123",
                "expiryDate", "12/2025",
                "cardHolderName", "Overlap Test User"
            );
        }).iterator();

    // Scenario 1: All users try to book the exact same seat
    ScenarioBuilder sameSeatRace = scenario("Same Seat Race Condition")
        .feed(sameSeatsFeeder)
        // Immediate reservation attempt
        .exec(http("Reserve Same Seat")
            .post("/api/bookings/reserve")
            .body(StringBody("""
                {
                    "eventId": #{eventId},
                    "seatNumbers": ["#{seatNumber}"],
                    "userId": "#{userId}"
                }
                """))
            .check(status().in(200, 400, 409))
            .check(jsonPath("$.success").exists())
            .check(jsonPath("$.success").saveAs("reservationSuccess"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.reservationId").saveAs("reservationId"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.totalAmount").saveAs("totalAmount"))
        )
        // Only proceed to payment if reservation was successful
        .doIf(session -> "true".equals(session.getString("reservationSuccess")))
        .then(
            pause(Duration.ofMillis(100), Duration.ofMillis(500))
                .exec(http("Confirm Booking for Same Seat")
                    .post("/api/bookings/confirm")
                    .body(StringBody("""
                        {
                            "reservationId": #{reservationId},
                            "userId": "#{userId}",
                            "paymentRequest": {
                                "paymentMethod": "CREDIT_CARD",
                                "cardNumber": "#{cardNumber}",
                                "amount": #{totalAmount},
                                "cardHolderName": "#{cardHolderName}",
                                "cvv": "#{cvv}",
                                "expiryDate": "#{expiryDate}"
                            }
                        }
                        """))
                    .check(status().in(200, 400, 409))
                    .check(jsonPath("$.success").exists())
                )
        );

    // Scenario 2: Users compete for a limited pool of seats
    ScenarioBuilder overlappingSeatsRace = scenario("Overlapping Seats Race Condition")
        .feed(overlappingSeatsFeeder)
        .exec(http("Reserve Overlapping Seat")
            .post("/api/bookings/reserve")
            .body(StringBody("""
                {
                    "eventId": #{eventId},
                    "seatNumbers": ["#{seatNumber}"],
                    "userId": "#{userId}"
                }
                """))
            .check(status().in(200, 400, 409))
            .check(jsonPath("$.success").exists())
            .check(jsonPath("$.success").saveAs("reservationSuccess"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.reservationId").saveAs("reservationId"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.totalAmount").saveAs("totalAmount"))
        )
        .doIf(session -> "true".equals(session.getString("reservationSuccess")))
        .then(
            pause(Duration.ofMillis(50), Duration.ofMillis(300))
                .exec(http("Confirm Booking for Overlapping Seat")
                    .post("/api/bookings/confirm")
                    .body(StringBody("""
                        {
                            "reservationId": #{reservationId},
                            "userId": "#{userId}",
                            "paymentRequest": {
                                "paymentMethod": "CREDIT_CARD",
                                "cardNumber": "#{cardNumber}",
                                "amount": #{totalAmount},
                                "cardHolderName": "#{cardHolderName}",
                                "cvv": "#{cvv}",
                                "expiryDate": "#{expiryDate}"
                            }
                        }
                        """))
                    .check(status().in(200, 400, 409))
                    .check(jsonPath("$.success").exists())
                )
        );

    // Scenario 3: Burst load - All users hit at the exact same time
    ScenarioBuilder burstLoad = scenario("Burst Load Race Condition")
        .feed(overlappingSeatsFeeder)
        .exec(http("Burst Reserve")
            .post("/api/bookings/reserve")
            .body(StringBody("""
                {
                    "eventId": #{eventId},
                    "seatNumbers": ["#{seatNumber}"],
                    "userId": "#{userId}"
                }
                """))
            .check(status().in(200, 400, 409))
            .check(jsonPath("$.success").exists())
        );

    // Setup simulation
    {
        setUp(
            // Test 1: 50 users trying to book the exact same seat at once
            sameSeatRace.injectOpen(
                atOnceUsers(50),
                // Wait a bit, then do it again
                nothingFor(Duration.ofSeconds(5)),
                atOnceUsers(30)
            ),

            // Test 2: 100 users competing for 10 seats with gradual ramp
            overlappingSeatsRace.injectOpen(
                rampUsers(CONCURRENT_USERS).during(Duration.ofSeconds(2))
            ),

            // Test 3: Massive burst - 200 users at the exact same moment
            burstLoad.injectOpen(
                nothingFor(Duration.ofSeconds(10)),
                atOnceUsers(200)
            )
        ).protocols(httpProtocol)
         .assertions(
             // We expect some failures due to race conditions (conflicts)
             global().failedRequests().percent().lt(80.0), // At most 80% should fail
             global().responseTime().max().lt(10000), // No request should take more than 10s
             forAll().responseTime().percentile3().lt(5000) // 99.9th percentile under 5s
         );
    }
}
