# Test Data Guide

## Overview

This guide explains how test data is managed and used in the ticket booking system's unit and integration tests.

## Test Data Loading

### Automatic Loading with `import.sql`

The test database is automatically populated when tests run using Hibernate's `import.sql` feature:

- **Location**: `src/test/resources/import.sql`
- **Execution**: Automatically runs after schema creation
- **Trigger**: Each test execution with `@QuarkusTest`
- **Database Strategy**: `create-drop` (recreates schema each run)

### Test Data Overview

The `import.sql` file creates a minimal but comprehensive test dataset:

#### Events (3 Total)

| Event ID | Name | Venue | Total Seats | Type |
|----------|------|-------|-------------|------|
| 1 | Rock Concert 2025 | Stadium Arena | 20 | General testing |
| 2 | Broadway Show | City Theater | 15 | Different pricing |
| 3 | Championship Game | Sports Complex | 10 | Edge cases |

#### Seats

**Event 1 (Rock Concert)** - 20 seats:
- 5 VIP seats: A1-A5 @ $299.99
- 10 Premium seats: B1-B5, C1-C5 @ $149.99
- 5 Regular seats: D1-D5 @ $79.99

**Event 2 (Broadway Show)** - 15 seats:
- 3 VIP seats: A1-A3 @ $199.99
- 5 Premium seats: B1-B5 @ $99.99
- 7 Regular seats: C1-C7 @ $49.99

**Event 3 (Championship Game)** - 10 seats:
- 2 VIP seats: A1-A2 @ $399.99
- 3 Premium seats: B1-B3 @ $199.99
- 5 Regular seats: C1-C5 @ $99.99

## Using Test Data in Tests

### Using `TestDataHelper` Class

The `TestDataHelper` class provides constants for all test data:

```java
import com.ticketing.TestDataHelper;
import com.ticketing.TestDataHelper.Event1Seats;
import com.ticketing.TestDataHelper.SeatNumbers;
import com.ticketing.TestDataHelper.Prices;
import com.ticketing.TestDataHelper.TestUsers;

@QuarkusTest
public class MyServiceTest {

    @Inject
    EventRepository eventRepository;

    @Test
    public void testFindEvent() {
        // Use predefined event ID
        Event event = eventRepository.findById(TestDataHelper.EVENT_ROCK_CONCERT_ID);

        assertNotNull(event);
        assertEquals(TestDataHelper.EVENT_ROCK_CONCERT_NAME, event.getEventName());
        assertEquals(TestDataHelper.VENUE_STADIUM_ARENA, event.getVenueName());
    }

    @Test
    public void testReserveSeat() {
        // Use predefined seat ID and number
        ReservationRequest request = ReservationRequest.builder()
            .eventId(TestDataHelper.EVENT_ROCK_CONCERT_ID)
            .userId(TestDataHelper.TestUsers.USER_1)
            .seatNumbers(List.of(SeatNumbers.A1, SeatNumbers.A2))
            .build();

        ReservationResponse response = reservationService.reserveSeats(request);

        assertNotNull(response);
        assertEquals(2, response.getSeats().size());
        assertEquals(Prices.EVENT_1_VIP.multiply(new BigDecimal("2")),
                     response.getTotalAmount());
    }
}
```

### TestDataHelper Constants

#### Event IDs
```java
TestDataHelper.EVENT_ROCK_CONCERT_ID    // 1L
TestDataHelper.EVENT_THEATER_SHOW_ID    // 2L
TestDataHelper.EVENT_SPORTS_GAME_ID     // 3L
```

#### Seat IDs (Event 1)
```java
Event1Seats.VIP_A1        // 1L
Event1Seats.VIP_A2        // 2L
Event1Seats.PREMIUM_B1    // 6L
Event1Seats.REGULAR_D1    // 16L
```

#### Seat Numbers
```java
SeatNumbers.A1   // "A1"
SeatNumbers.B5   // "B5"
SeatNumbers.C3   // "C3"
SeatNumbers.D1   // "D1"
```

#### Prices
```java
Prices.EVENT_1_VIP        // 299.99
Prices.EVENT_1_PREMIUM    // 149.99
Prices.EVENT_1_REGULAR    // 79.99
```

#### Test Users
```java
TestUsers.USER_1       // "user-test-001"
TestUsers.USER_2       // "user-test-002"
TestUsers.USER_ADMIN   // "admin-test-001"
```

#### Test Payment Data
```java
TestPayments.CARD_NUMBER_VALID    // "4111111111111111"
TestPayments.CVV_VALID            // "123"
TestPayments.CARDHOLDER_NAME      // "Test User"
String expiry = TestPayments.getValidExpiryDate();  // Future date
```

### Utility Methods

```java
// Get dates
LocalDateTime futureDate = TestDataHelper.getFutureEventDate();  // +30 days
LocalDateTime pastDate = TestDataHelper.getPastEventDate();      // -30 days

// Get reservation times
LocalDateTime validExpiry = TestDataHelper.getReservationExpiryTime();    // +10 min
LocalDateTime expiredTime = TestDataHelper.getExpiredReservationTime();   // -10 min
```

## Example Tests

### Test Seat Reservation

```java
@QuarkusTest
public class ReservationServiceTest {

    @Inject
    ReservationService reservationService;

    @Test
    public void shouldReserveAvailableSeats() {
        ReservationRequest request = ReservationRequest.builder()
            .eventId(TestDataHelper.EVENT_ROCK_CONCERT_ID)
            .userId(TestDataHelper.TestUsers.USER_1)
            .seatNumbers(List.of(SeatNumbers.A1, SeatNumbers.A2))
            .build();

        ReservationResponse response = reservationService.reserveSeats(request);

        assertNotNull(response);
        assertEquals(2, response.getSeats().size());
        assertNotNull(response.getExpiresAt());
    }
}
```

### Test Booking Confirmation

```java
@QuarkusTest
@Transactional
public class BookingServiceTest {

    @Inject
    BookingService bookingService;

    @Inject
    ReservationService reservationService;

    @Test
    public void shouldConfirmBookingWithValidPayment() {
        // First create a reservation
        ReservationRequest reservationRequest = ReservationRequest.builder()
            .eventId(TestDataHelper.EVENT_ROCK_CONCERT_ID)
            .userId(TestDataHelper.TestUsers.USER_1)
            .seatNumbers(List.of(SeatNumbers.A1))
            .build();

        ReservationResponse reservation = reservationService
            .reserveSeats(reservationRequest);

        // Create payment request
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .cardNumber(TestPayments.CARD_NUMBER_VALID)
            .cvv(TestPayments.CVV_VALID)
            .expiryDate(TestPayments.getValidExpiryDate())
            .cardholderName(TestPayments.CARDHOLDER_NAME)
            .build();

        // Confirm booking
        BookingConfirmRequest confirmRequest = BookingConfirmRequest.builder()
            .reservationId(reservation.getReservationId())
            .userId(TestDataHelper.TestUsers.USER_1)
            .paymentRequest(paymentRequest)
            .build();

        BookingResponse booking = bookingService.confirmBooking(confirmRequest);

        assertNotNull(booking);
        assertEquals("CONFIRMED", booking.getStatus());
        assertNotNull(booking.getBookingReference());
    }
}
```

### Test Race Conditions

```java
@QuarkusTest
public class ConcurrentReservationTest {

    @Inject
    ReservationService reservationService;

    @Test
    public void shouldHandleConcurrentReservations() throws Exception {
        String seatNumber = SeatNumbers.A1;

        // Try to reserve the same seat from two different users
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable reservationTask = (userId) -> {
            try {
                ReservationRequest request = ReservationRequest.builder()
                    .eventId(TestDataHelper.EVENT_ROCK_CONCERT_ID)
                    .userId(userId)
                    .seatNumbers(List.of(seatNumber))
                    .build();

                reservationService.reserveSeats(request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        // Execute concurrent reservations
        new Thread(() -> reservationTask.run(TestUsers.USER_1)).start();
        new Thread(() -> reservationTask.run(TestUsers.USER_2)).start();

        latch.await(10, TimeUnit.SECONDS);

        // Only one should succeed
        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());
    }
}
```

## Test Configuration

### Database Settings (`src/test/resources/application.yml`)

```yaml
quarkus:
  hibernate-orm:
    database:
      generation: create-drop  # Recreates schema each run
    log:
      sql: false  # Set to true for debugging
```

### Enabling SQL Logging

To debug test data issues, enable SQL logging:

```yaml
quarkus:
  hibernate-orm:
    log:
      sql: true
      format-sql: true
```

## Customizing Test Data

### Adding More Test Data

To add more test data, edit `src/test/resources/import.sql`:

```sql
-- Add a new event
INSERT INTO events (event_id, event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, version, created_at)
VALUES (4, 'My Test Event', CURRENT_TIMESTAMP + INTERVAL '90 days', 'Test Venue', 5, 5, 'ON_SALE', CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP);

-- Add seats for the new event
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (46, 4, 'A1', 'VIP', 'A', 'VIP', 500.00, 'AVAILABLE', 0, CURRENT_TIMESTAMP);
```

### Updating TestDataHelper

Add corresponding constants to `TestDataHelper.java`:

```java
public static final Long EVENT_MY_TEST_ID = 4L;
public static final String EVENT_MY_TEST_NAME = "My Test Event";
```

## Best Practices

### 1. Use Constants

Always use `TestDataHelper` constants instead of hardcoding values:

```java
// Good
Long eventId = TestDataHelper.EVENT_ROCK_CONCERT_ID;

// Bad
Long eventId = 1L;
```

### 2. Keep Test Data Minimal

Only include data necessary for tests. The current dataset is intentionally small for:
- Fast test execution
- Easy debugging
- Clear test intentions

### 3. Avoid Test Data Coupling

Don't rely on specific database state between tests:

```java
// Good - each test is independent
@Test
public void testA() {
    // Setup test data
    // Run test
    // Verify results
}

@Test
public void testB() {
    // Setup test data (independent from testA)
    // Run test
    // Verify results
}
```

### 4. Use Transactions for Cleanup

Use `@Transactional` to ensure test data is rolled back:

```java
@QuarkusTest
@Transactional
public class MyTest {
    @Test
    public void testSomething() {
        // Changes will be rolled back after test
    }
}
```

## Comparison: Test Data vs Production Data

| Aspect | Test Data (`import.sql`) | Production Data (`init-sample-data.sh`) |
|--------|-------------------------|----------------------------------------|
| **Size** | Minimal (45 seats) | Large (300 seats) |
| **Events** | 3 events | 3 events |
| **Purpose** | Unit/Integration tests | Manual testing, demos |
| **Loading** | Automatic on test run | Manual execution |
| **Speed** | Very fast | Fast |
| **Cleanup** | Automatic (create-drop) | Manual |

## Troubleshooting

### Tests Fail with "Data Not Found"

**Problem**: Tests can't find expected data.

**Solution**:
1. Verify `import.sql` is in `src/test/resources/`
2. Check SQL syntax is correct
3. Enable SQL logging to see what's being executed
4. Verify sequence resets are working

### Data Persists Between Tests

**Problem**: Data from one test affects another.

**Solution**:
1. Use `@Transactional` annotation
2. Verify `database.generation: create-drop` is set
3. Don't commit transactions in tests unless necessary

### Slow Test Execution

**Problem**: Tests take too long to run.

**Solution**:
1. Minimize test data in `import.sql`
2. Use `@BeforeAll` instead of `@BeforeEach` where possible
3. Disable SQL logging in tests
4. Use in-memory H2 database for unit tests

## See Also

- [Project Structure](PROJECT_STRUCTURE.md)
- [Testing Strategy](README_SCRIPTS.md)
- [Sample Data Script](INIT_DATA_SCRIPT.md)

---

**Last Updated**: 2025-11-07
**Author**: Development Team
