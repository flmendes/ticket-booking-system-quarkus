# Global Exception Handling with RFC 7807 Problem Details

## Overview

This project implements a **Global Exception Handler** using JAX-RS `ExceptionMapper` with full support for **RFC 7807 Problem Details** for HTTP APIs. This provides a consistent, standardized way to handle errors across the entire application.

## RFC 7807 - Problem Details

RFC 7807 defines a standard format for representing errors in HTTP APIs:

```json
{
  "type": "https://example.com/problems/reservation-expired",
  "title": "Reservation Expired",
  "status": 410,
  "detail": "Reservation expired. Please select seats again.",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "reason": "expired"
  }
}
```

### Standard Fields

- **type**: URI identifying the problem type
- **title**: Short, human-readable summary of the problem
- **status**: HTTP status code
- **detail**: Explanation specific to this occurrence
- **timestamp**: When the error occurred
- **extensions**: Additional custom properties

## Architecture

### 1. ProblemDetail Class

Located at: `com.ticketing.shared.dto.ProblemDetail`

The core class representing RFC 7807 Problem Details:

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {
    private URI type;
    private String title;
    private Integer status;
    private String detail;
    private URI instance;
    private LocalDateTime timestamp;
    private Map<String, Object> extensions;
}
```

### 2. Exception Mappers

All exception mappers are located in: `com.ticketing.shared.exception.handler`

#### Business Exception Mappers

| Mapper | Exception | HTTP Status | Problem Type |
|--------|-----------|-------------|--------------|
| `EventNotFoundExceptionMapper` | `EventNotFoundException` | 404 | `/problems/event-not-found` |
| `SeatNotFoundExceptionMapper` | `SeatNotFoundException` | 404 | `/problems/seat-not-found` |
| `SeatNotAvailableExceptionMapper` | `SeatNotAvailableException` | 409 | `/problems/seat-not-available` |
| `ReservationNotFoundExceptionMapper` | `ReservationNotFoundException` | 404 | `/problems/reservation-not-found` |
| `ReservationExpiredExceptionMapper` | `ReservationExpiredException` | 410 | `/problems/reservation-expired` |
| `InvalidReservationExceptionMapper` | `InvalidReservationException` | 400 | `/problems/invalid-reservation` |
| `PaymentFailedExceptionMapper` | `PaymentFailedException` | 402 | `/problems/payment-failed` |
| `InvalidSeatStateExceptionMapper` | `InvalidSeatStateException` | 409 | `/problems/invalid-seat-state` |
| `BookingFailureExceptionMapper` | `BookingFailureException` | 500 | `/problems/booking-failure` |
| `ConcurrentModificationExceptionMapper` | `ConcurrentModificationException` | 409 | `/problems/concurrent-modification` |

#### System Exception Mappers

| Mapper | Exception | HTTP Status | Description |
|--------|-----------|-------------|-------------|
| `ValidationExceptionMapper` | `ConstraintViolationException` | 400 | Bean Validation errors |
| `NotFoundExceptionMapper` | `NotFoundException` | 404 | Generic JAX-RS not found |
| `GenericExceptionMapper` | `Exception` | 500 | Catch-all for unhandled exceptions |

### 3. How It Works

#### Before (Manual Exception Handling)

```java
@POST
public Response reserveSeats(@Valid ReservationRequest request) {
    try {
        ReservationResponse response = reservationService.reserveSeats(request);
        return Response.ok(ApiResponse.success(response, "Success")).build();
    } catch (SeatNotFoundException e) {
        return Response.status(404)
            .entity(ApiResponse.error(e.getMessage()))
            .build();
    } catch (SeatNotAvailableException e) {
        return Response.status(409)
            .entity(ApiResponse.error(e.getMessage()))
            .build();
    } catch (Exception e) {
        return Response.status(500)
            .entity(ApiResponse.error("Internal error"))
            .build();
    }
}
```

#### After (With Exception Mappers)

```java
@POST
public Response reserveSeats(@Valid ReservationRequest request) {
    ReservationResponse response = reservationService.reserveSeats(request);
    return Response.ok(ApiResponse.success(response, "Success")).build();
}
```

Exceptions are automatically caught and converted to Problem Details!

## Examples

### 1. Seat Not Found (404)

**Request:**
```http
GET /api/events/1/seats/available
```

**Response:**
```json
{
  "type": "/problems/seat-not-found",
  "title": "Seat Not Found",
  "status": 404,
  "detail": "Seat A1 not found",
  "timestamp": "2025-11-05T15:30:00"
}
```

### 2. Seat Not Available (409 Conflict)

**Request:**
```http
POST /api/bookings/reserve
{
  "eventId": 1,
  "seatNumbers": ["A1"],
  "userId": "user123"
}
```

**Response:**
```json
{
  "type": "/problems/seat-not-available",
  "title": "Seat Not Available",
  "status": 409,
  "detail": "Seat A1 is no longer available",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "reason": "conflict"
  }
}
```

### 3. Reservation Expired (410 Gone)

**Request:**
```http
POST /api/bookings/confirm
{
  "reservationId": 123,
  "userId": "user123",
  "paymentRequest": { ... }
}
```

**Response:**
```json
{
  "type": "/problems/reservation-expired",
  "title": "Reservation Expired",
  "status": 410,
  "detail": "Reservation expired. Please select seats again.",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "reason": "expired"
  }
}
```

### 4. Validation Error (400)

**Request:**
```http
POST /api/bookings/reserve
{
  "eventId": null,
  "seatNumbers": [],
  "userId": ""
}
```

**Response:**
```json
{
  "type": "/problems/validation-error",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields failed validation",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "violations": {
      "eventId": "Event ID is required",
      "seatNumbers": "At least one seat must be selected",
      "userId": "User ID is required"
    }
  }
}
```

### 5. Payment Failed (402 Payment Required)

**Request:**
```http
POST /api/bookings/confirm
```

**Response:**
```json
{
  "type": "/problems/payment-failed",
  "title": "Payment Failed",
  "status": 402,
  "detail": "Payment processing failed: Card declined",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "reason": "payment_processing_failed"
  }
}
```

### 6. Concurrent Modification (409)

**Response:**
```json
{
  "type": "/problems/concurrent-modification",
  "title": "Concurrent Modification",
  "status": 409,
  "detail": "Seat was modified by another user. Please try again.",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "reason": "optimistic_lock_failure",
    "retry_recommended": true
  }
}
```

### 7. Internal Server Error (500)

**Response (Development Mode):**
```json
{
  "type": "/problems/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "NullPointerException: Cannot invoke method on null",
  "timestamp": "2025-11-05T15:30:00",
  "extensions": {
    "exception_type": "java.lang.NullPointerException"
  }
}
```

**Response (Production Mode):**
```json
{
  "type": "/problems/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred. Please try again later.",
  "timestamp": "2025-11-05T15:30:00"
}
```

## Benefits

### 1. **Consistency**
- All errors follow the same RFC 7807 format
- Predictable structure for clients

### 2. **Separation of Concerns**
- Controllers focus on business logic
- Exception handling is centralized

### 3. **Maintainability**
- Easy to add new exception types
- No duplicated error handling code

### 4. **Client-Friendly**
- Structured error responses
- Machine-readable error types
- Human-readable messages

### 5. **Standards Compliance**
- Follows RFC 7807
- Industry best practices

### 6. **Security**
- Sensitive details hidden in production
- Stack traces not exposed

## HTTP Status Codes

| Status Code | Description | Use Case |
|-------------|-------------|----------|
| 400 | Bad Request | Validation errors, invalid input |
| 402 | Payment Required | Payment processing failures |
| 404 | Not Found | Resource not found |
| 409 | Conflict | State conflicts, concurrent modifications |
| 410 | Gone | Expired resources |
| 500 | Internal Server Error | Unexpected errors |

## Configuration

### Development vs Production

The `GenericExceptionMapper` behaves differently based on the Quarkus profile:

**Development (`quarkus.profile=dev`):**
- Full error details exposed
- Exception types shown
- Stack traces in logs

**Production (`quarkus.profile=prod`):**
- Generic error messages
- No internal details exposed
- Security-focused

## Testing Exception Handlers

Example test cases:

```java
@QuarkusTest
public class ExceptionHandlerTest {

    @Test
    public void testEventNotFoundException() {
        given()
            .when().get("/api/events/99999")
            .then()
            .statusCode(404)
            .body("type", containsString("/problems/event-not-found"))
            .body("title", equalTo("Event Not Found"))
            .body("status", equalTo(404));
    }

    @Test
    public void testValidationError() {
        given()
            .contentType("application/json")
            .body("{\"eventId\": null}")
            .when().post("/api/bookings/reserve")
            .then()
            .statusCode(400)
            .body("type", containsString("/problems/validation-error"))
            .body("extensions.violations", hasKey("eventId"));
    }
}
```

## Best Practices

1. **Always throw specific exceptions** in services
2. **Let exceptions propagate** to ExceptionMappers
3. **Don't catch exceptions in controllers** unless you have a specific reason
4. **Use meaningful error messages** for the `detail` field
5. **Add extensions** for additional context
6. **Log appropriately** in exception mappers

## References

- [RFC 7807 - Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807)
- [JAX-RS ExceptionMapper](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ExceptionMapper.html)
- [Quarkus REST Exception Handling](https://quarkus.io/guides/rest-json#exception-handling)
