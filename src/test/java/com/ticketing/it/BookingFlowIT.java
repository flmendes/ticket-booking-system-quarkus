package com.ticketing.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class BookingFlowIT {

    @Test
    void should_list_events_and_available_seats() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/events")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data", notNullValue());

        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/events/1/seats/available")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data", notNullValue());
    }

    @Test
    void should_reserve_two_seats_and_confirm_booking() {
        // Reserve seats A1 and A2
        var reservePayload = "{\n" +
                "  \"eventId\": 1,\n" +
                "  \"seatNumbers\": [\"A1\", \"A2\"],\n" +
                "  \"userId\": \"user123\"\n" +
                "}";

        var reserveResponse =
            given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(reservePayload)
            .when()
                .post("/api/bookings/reserve")
            .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.reservationId", notNullValue())
                .extract().path("data");

        Integer reservationId = ((Number)((java.util.Map<?,?>)reserveResponse).get("reservationId")).intValue();
        java.math.BigDecimal totalAmount = new java.math.BigDecimal(((java.util.Map<?,?>)reserveResponse).get("totalAmount").toString());

        // Confirm booking
        String confirmPayload = "{\n" +
                "  \"reservationId\": " + reservationId + ",\n" +
                "  \"userId\": \"user123\",\n" +
                "  \"paymentRequest\": {\n" +
                "    \"paymentMethod\": \"CREDIT_CARD\",\n" +
                "    \"cardNumber\": \"4111111111111111\",\n" +
                "    \"cardHolderName\": \"John Doe\",\n" +
                "    \"amount\": " + totalAmount + ",\n" +
                "    \"cvv\": \"123\",\n" +
                "    \"expiryDate\": \"12/26\"\n" +
                "  }\n" +
                "}";

        var bookingRef =
            given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(confirmPayload)
            .when()
                .post("/api/bookings/confirm")
            .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.bookingReference", not(isEmptyOrNullString()))
                .extract().path("data.bookingReference");

        // Fetch booking by reference
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/bookings/reference/" + bookingRef)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.bookingReference", equalTo(bookingRef));
    }
}

