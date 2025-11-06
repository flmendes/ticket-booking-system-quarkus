package com.ticketing.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class RaceConditionIT {

    @Test
    void should_allow_only_one_successful_reservation_for_same_seat_concurrently() throws Exception {
        final String seat = "B5";
        final int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                String payload = "{\n" +
                        "  \"eventId\": 1,\n" +
                        "  \"seatNumbers\": [\"" + seat + "\"],\n" +
                        "  \"userId\": \"concurrent_user_" + idx + "\"\n" +
                        "}";

                Boolean success =
                    given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(payload)
                    .when()
                        .post("/api/bookings/reserve")
                    .then()
                        .statusCode(200)
                        .extract().path("success");

                return Boolean.TRUE.equals(success);
            }));
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        int successes = 0;
        for (Future<Boolean> f : futures) {
            if (Boolean.TRUE.equals(f.get())) successes++;
        }

        assertThat("Exactly one reservation must succeed", successes, is(1));
    }
}

