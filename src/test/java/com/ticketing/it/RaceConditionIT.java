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
        final String seat = "C1"; // distinct seat to avoid interference with other race test
        final int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                String payload = "{\n" +
                        "  \"eventId\": 1,\n" +
                        "  \"seatNumbers\": [\"" + seat + "\"],\n" +
                        "  \"userId\": \"concurrent_user_it_" + idx + "\"\n" +
                        "}";

                var response =
                    given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body(payload)
                    .when()
                        .post("/api/bookings/reserve");

                int status = response.statusCode();
                if (status == 200) {
                    Boolean success = response.then().extract().path("success");
                    return Boolean.TRUE.equals(success);
                } else if (status == 409) {
                    return false; // expected contention failure
                } else {
                    return false; // any other status counts as failure
                }
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
