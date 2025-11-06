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
 * Stress Test Simulation
 * Tests system behavior under extreme load conditions
 */
public class StressTestSimulation extends Simulation {

    // Configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int NORMAL_LOAD = Integer.parseInt(System.getProperty("normalLoad", "10"));
    private static final int STRESS_LOAD = Integer.parseInt(System.getProperty("stressLoad", "100"));
    private static final int SPIKE_LOAD = Integer.parseInt(System.getProperty("spikeLoad", "500"));

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Stress Test")
        .shareConnections(); // Share connections for more realistic load

    // Feeder for generating diverse test data
    // Uses actual event IDs and seat patterns from seed data
    Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            long eventId = random.nextLong(1, 6); // Events 1-5
            String seatNumber;

            // Generate seat numbers based on event ID to match seed data
            switch ((int) eventId) {
                case 1:
                    seatNumber = random.nextBoolean() ? "A" + random.nextInt(1, 101) : "D" + random.nextInt(1, 301);
                    break;
                case 2:
                    seatNumber = "A" + random.nextInt(1, 301);
                    break;
                case 3:
                    seatNumber = "SEAT-" + random.nextInt(1, 401);
                    break;
                case 4:
                    seatNumber = "S" + random.nextInt(1, 201);
                    break;
                case 5:
                    seatNumber = "L" + random.nextInt(1, 601);
                    break;
                default:
                    seatNumber = "A1";
            }

            return Map.of(
                "userId", "stress-user-" + random.nextInt(10000, 99999),
                "eventId", eventId,
                "seatNumber", seatNumber,
                "cardNumber", "4111111111111111",
                "cvv", String.format("%03d", random.nextInt(100, 1000)),
                "expiryDate", String.format("%02d/20%02d", random.nextInt(1, 13), random.nextInt(25, 31)),
                "cardHolderName", "Stress Test User " + random.nextInt(1000)
            );
        }).iterator();

    // Full booking flow scenario
    ScenarioBuilder fullBookingFlow = scenario("Full Booking Flow Under Stress")
        .feed(feeder)
        .exec(http("Reserve Seats")
            .post("/api/bookings/reserve")
            .body(StringBody("""
                {
                    "eventId": #{eventId},
                    "seatNumbers": ["#{seatNumber}"],
                    "userId": "#{userId}"
                }
                """))
            .check(status().in(200, 400, 409, 500, 503))
            .check(jsonPath("$.success").saveAs("reservationSuccess"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.reservationId").saveAs("reservationId"))
            .checkIf(session -> "true".equals(session.getString("reservationSuccess")))
                .then(jsonPath("$.data.totalAmount").saveAs("totalAmount"))
        )
        .pause(Duration.ofMillis(100), Duration.ofMillis(500))
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
                .check(status().in(200, 400, 409, 500, 503))
            )
        );

    // Read operations scenario
    ScenarioBuilder readOperations = scenario("Read Operations Under Load")
        .feed(feeder)
        .randomSwitch().on(
            percent(50.0).then(exec(http("Get User Bookings")
                .get("/api/bookings/user/#{userId}")
                .check(status().in(200, 404, 500, 503))
            )),
            percent(30.0).then(exec(http("Health Check")
                .get("/api/bookings/health")
                .check(status().in(200, 500, 503))
            )),
            percent(20.0).then(exec(http("Get Booking by Reference")
                .get("/api/bookings/reference/BK-#{userId}")
                .check(status().in(200, 404, 500, 503))
            ))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(2));

    // Setup: Progressive load with spikes
    {
        setUp(
            // Progressive load phases for full booking flow
            fullBookingFlow.injectOpen(
                // Phase 1: Normal load (baseline)
                nothingFor(Duration.ofSeconds(5)),
                constantUsersPerSec(NORMAL_LOAD).during(Duration.ofSeconds(30)),
                // Phase 2: Gradual stress increase
                rampUsersPerSec(NORMAL_LOAD).to(STRESS_LOAD).during(Duration.ofSeconds(60)),
                // Phase 3: Sustained stress
                constantUsersPerSec(STRESS_LOAD).during(Duration.ofSeconds(120)),
                // Phase 4: Spike load
                rampUsersPerSec(STRESS_LOAD).to(SPIKE_LOAD).during(Duration.ofSeconds(10)),
                constantUsersPerSec(SPIKE_LOAD).during(Duration.ofSeconds(30)),
                // Phase 5: Recovery (ramp down)
                rampUsersPerSec(SPIKE_LOAD).to(NORMAL_LOAD).during(Duration.ofSeconds(60)),
                constantUsersPerSec(NORMAL_LOAD).during(Duration.ofSeconds(30))
            ),

            // Continuous read operations throughout the test
            readOperations.injectOpen(
                nothingFor(Duration.ofSeconds(10)),
                rampUsersPerSec(5).to(50).during(Duration.ofSeconds(60)),
                constantUsersPerSec(50).during(Duration.ofSeconds(240)),
                rampUsersPerSec(50).to(5).during(Duration.ofSeconds(30))
            )
        ).protocols(httpProtocol)
         .assertions(
             // During stress, we allow higher failure rates
             global().failedRequests().percent().lt(30.0),
             global().responseTime().percentile4().lt(30000), // 99.99th percentile
             // At least some requests should succeed
             global().successfulRequests().count().gt(100L)
         )
         .maxDuration(Duration.ofMinutes(10)); // Safety limit
    }
}
