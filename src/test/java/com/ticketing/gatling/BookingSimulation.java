package com.ticketing.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling simulation for testing concurrent booking requests
 * This simulates realistic user behavior with reservation and booking confirmation
 */
public class BookingSimulation extends Simulation {

    // Configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int CONCURRENT_USERS = Integer.parseInt(System.getProperty("users", "50"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("rampDuration", "10"));
    private static final int TEST_DURATION = Integer.parseInt(System.getProperty("duration", "60"));

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Load Test");

    // Feeder for generating unique user IDs and seat numbers
    // Uses realistic data that matches the seed script (Events 1-5, various seat patterns)
    Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            long eventId = random.nextLong(1, 6); // Events 1-5
            String seatNumber;

            // Generate seat numbers based on event ID to match seed data
            switch ((int) eventId) {
                case 1: // Event 1: A1-A100, VIP-1 to VIP-50, PREM-1 to PREM-50, D1-D300
                    int seatType = random.nextInt(4);
                    if (seatType == 0) {
                        seatNumber = "A" + random.nextInt(1, 101);
                    } else if (seatType == 1) {
                        seatNumber = "VIP-" + random.nextInt(1, 51);
                    } else if (seatType == 2) {
                        seatNumber = "PREM-" + random.nextInt(1, 51);
                    } else {
                        seatNumber = "D" + random.nextInt(1, 301);
                    }
                    break;
                case 2: // Event 2: A1-A300
                    seatNumber = "A" + random.nextInt(1, 301);
                    break;
                case 3: // Event 3: SEAT-1 to SEAT-400
                    seatNumber = "SEAT-" + random.nextInt(1, 401);
                    break;
                case 4: // Event 4: S1-S200
                    seatNumber = "S" + random.nextInt(1, 201);
                    break;
                case 5: // Event 5: L1-L600
                    seatNumber = "L" + random.nextInt(1, 601);
                    break;
                default:
                    seatNumber = "A" + random.nextInt(1, 101);
            }

            return Map.of(
                "userId", "user-" + random.nextInt(10000, 99999),
                "eventId", eventId,
                "seatNumber", seatNumber,
                "cardNumber", "4111111111111111",
                "cvv", "123",
                "expiryDate", "12/2025",
                "cardHolderName", "Test User"
            );
        }).iterator();

    // Scenario 1: Complete Booking Flow
    ScenarioBuilder completeBookingFlow = scenario("Complete Booking Flow")
        .feed(feeder)
        // Step 1: Reserve seats
        .exec(http("Reserve Seats")
            .post("/api/bookings/reserve")
            .body(StringBody("""
                {
                    "eventId": #{eventId},
                    "seatNumbers": ["#{seatNumber}"],
                    "userId": "#{userId}"
                }
                """))
            .check(status().in(200, 400, 409))
            .check(jsonPath("$.success").saveAs("reservationSuccess"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.reservationId").saveAs("reservationId"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.totalAmount").saveAs("totalAmount"))
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))

        // Step 2: Confirm booking with payment (only if reservation was successful)
        .doIf(session -> "true".equals(session.getString("reservationSuccess")))
        .then(
            exec(http("Confirm Booking")
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
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(2));

    // Scenario 2: Read-Heavy - Get User Bookings
    ScenarioBuilder getUserBookings = scenario("Get User Bookings")
        .feed(feeder)
        .exec(http("Get User Bookings")
            .get("/api/bookings/user/#{userId}")
            .check(status().is(200))
            .check(jsonPath("$.success").is("true"))
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3));

    // Scenario 3: Health Check
    ScenarioBuilder healthCheck = scenario("Health Check")
        .exec(http("Health Check")
            .get("/api/bookings/health")
            .check(status().is(200))
        )
        .pause(Duration.ofSeconds(5));

    // Setup simulation with multiple scenarios
    {
        setUp(
            // 70% of users do complete booking flow
            completeBookingFlow.injectOpen(
                rampUsers(CONCURRENT_USERS * 70 / 100).during(Duration.ofSeconds(RAMP_DURATION)),
                constantUsersPerSec(5).during(Duration.ofSeconds(TEST_DURATION))
            ),

            // 25% of users check their bookings
            getUserBookings.injectOpen(
                rampUsers(CONCURRENT_USERS * 25 / 100).during(Duration.ofSeconds(RAMP_DURATION)),
                constantUsersPerSec(2).during(Duration.ofSeconds(TEST_DURATION))
            ),

            // 5% health checks
            healthCheck.injectOpen(
                constantUsersPerSec(0.5).during(Duration.ofSeconds(TEST_DURATION + RAMP_DURATION))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),
             global().successfulRequests().percent().gt(90.0),
             forAll().responseTime().percentile3().lt(2000)
         );
    }
}
