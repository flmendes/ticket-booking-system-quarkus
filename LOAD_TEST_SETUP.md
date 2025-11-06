# Load Test Setup Guide

## Problem Analysis

The initial Gatling load tests failed with these errors:
- **50% - No reservationId attribute**: Reservation requests were failing
- **47% - 404 errors**: Seats and events didn't exist in the database
- **Only 36% success rate**: Far below the expected 90%

**Root Cause**: The tests were trying to book seats that don't exist in your database.

## Solution

We've implemented:
1. ✅ Database seed script with test data (2000+ seats across 5 events)
2. ✅ Improved error handling in Gatling simulations
3. ✅ Realistic data generators matching the seed data
4. ✅ Automated setup scripts

## Setup Instructions

### Step 1: Start Your Services

Make sure PostgreSQL, Redis, and the application are running:

```bash
# Start infrastructure (if using Docker Compose)
docker-compose up -d

# Or start services individually
# PostgreSQL on localhost:5432
# Redis on localhost:6379

# Start Quarkus application
./mvnw quarkus:dev
```

### Step 2: Seed Test Data

Run the seed script to create test events and seats:

```bash
# Default usage (uses postgres-ticket container from docker-compose.yml)
./seed-test-data.sh

# Or with custom container/database
POSTGRES_CONTAINER=my-postgres DB_NAME=mydb ./seed-test-data.sh
```

**Note**: The scripts use `docker exec` to run commands directly in the PostgreSQL container started by docker-compose. Make sure the containers are running first.

For detailed usage, see: [README_SCRIPTS.md](./README_SCRIPTS.md)

This will create:
- **5 test events** (Load Test Event 1-5)
- **2000+ seats** across all events with various types (VIP, PREMIUM, REGULAR)
- All events in **ON_SALE** status

**Verify the data was created:**
```bash
# Using psql
psql -h localhost -U postgres -d ticketing -c "
SELECT event_id, event_name, total_seats, available_seats, status
FROM events
WHERE event_name LIKE 'Load Test Event%';"
```

### Step 3: Run Load Tests

Now you can run the improved load tests:

```bash
# Booking flow test (recommended first test)
./run-gatling-booking.sh

# With custom parameters
./run-gatling-booking.sh -u 100 -d 120

# Race condition test
./run-gatling-race.sh -u 100

# Stress test
./run-gatling-stress.sh
```

## What Was Fixed

### 1. Data Generation
**Before:**
```java
"eventId", 1L,
"seatNumber", "A" + random.nextInt(1, 100)
```

**After:**
```java
long eventId = random.nextLong(1, 6); // Events 1-5 (actually exist)
// Event 1: A1-A100, VIP-1 to VIP-50, PREM-1 to PREM-50, D1-D300
// Event 2: A1-A300
// Event 3: SEAT-1 to SEAT-400
// Event 4: S1-S200
// Event 5: L1-L600
```

### 2. Error Handling
**Before:**
```java
.check(status().is(200))
.check(jsonPath("$.data.reservationId").saveAs("reservationId"))
// This fails if status is not 200
```

**After:**
```java
.check(status().in(200, 400, 409))
.check(jsonPath("$.success").saveAs("reservationSuccess"))
.checkIf(session -> "true".equals(session.getString("reservationSuccess")))
    .then(jsonPath("$.data.reservationId").saveAs("reservationId"))
.doIf(session -> "true".equals(session.getString("reservationSuccess")))
.then(
    exec(http("Confirm Booking")
        // Only runs if reservation was successful
    )
)
```

### 3. Realistic Test Data Structure

| Event ID | Seats Available | Seat Patterns | Types |
|----------|----------------|---------------|-------|
| 1 | 500 | A1-A100, VIP-1 to VIP-50, PREM-1 to PREM-50, D1-D300 | REGULAR, VIP, PREMIUM |
| 2 | 300 | A1-A300 | Mixed |
| 3 | 400 | SEAT-1 to SEAT-400 | REGULAR |
| 4 | 200 | S1-S200 | VIP, REGULAR |
| 5 | 600 | L1-L600 | Mixed |

## Expected Results After Fix

### Booking Flow Test
```bash
./run-gatling-booking.sh -u 50 -d 60
```

**Expected Metrics:**
- ✅ Success rate: > 85% (some conflicts are normal under load)
- ✅ P95 response time: < 500ms
- ✅ P99 response time: < 2000ms
- ✅ No "attribute not defined" errors
- ⚠️ Some 409 conflicts expected (multiple users trying same seat)

### Race Condition Test
```bash
./run-gatling-race.sh -u 100
```

**Expected Metrics:**
- ✅ High conflict rate (409 errors) - this is GOOD! It shows proper locking
- ✅ Only ONE booking succeeds per contested seat
- ✅ No double bookings
- ✅ Graceful handling of conflicts

### Stress Test
```bash
./run-gatling-stress.sh
```

**Expected Metrics:**
- ✅ Success rate: > 70% under stress
- ✅ System recovers during ramp-down phase
- ✅ No cascading failures
- ⚠️ Higher response times under spike load

## Troubleshooting

### Problem: Still getting 404 errors
**Solution:**
```bash
# Verify test data exists
psql -h localhost -U postgres -d ticketing -c "
SELECT COUNT(*) FROM seats WHERE status = 'AVAILABLE';"

# Re-run seed script if needed
./seed-test-data.sh
```

### Problem: All seats get booked quickly
**Solution:**
```bash
# Reset seat status between test runs
psql -h localhost -U postgres -d ticketing -c "
UPDATE seats
SET status = 'AVAILABLE',
    reserved_by = NULL,
    reserved_until = NULL,
    booking_id = NULL
WHERE event_id IN (SELECT event_id FROM events WHERE event_name LIKE 'Load Test Event%');"

# Also clean up test bookings
psql -h localhost -U postgres -d ticketing -c "
DELETE FROM booking_seats WHERE booking_id IN (
    SELECT booking_id FROM bookings
    WHERE user_id LIKE 'user-%'
       OR user_id LIKE 'race-user-%'
       OR user_id LIKE 'stress-user-%'
);
DELETE FROM bookings
WHERE user_id LIKE 'user-%'
   OR user_id LIKE 'race-user-%'
   OR user_id LIKE 'stress-user-%';

DELETE FROM reservations
WHERE user_id LIKE 'user-%'
   OR user_id LIKE 'race-user-%'
   OR user_id LIKE 'stress-user-%';"
```

### Problem: Connection refused
**Solution:**
```bash
# Check if application is running
curl http://localhost:8080/api/bookings/health

# Start application if not running
./mvnw quarkus:dev
```

### Problem: Database connection errors in seed script
**Solution:**
```bash
# Verify PostgreSQL is running
docker ps | grep postgres
# OR
pg_isready -h localhost -p 5432

# Check connection with psql
psql -h localhost -U postgres -d ticketing -c "SELECT 1;"
```

## Quick Reset Between Tests

Use this script to reset test data between runs:

```bash
# Create a reset script
cat > reset-test-data.sh << 'EOF'
#!/bin/bash
psql -h localhost -U postgres -d ticketing << SQL
-- Reset seats to AVAILABLE
UPDATE seats
SET status = 'AVAILABLE',
    reserved_by = NULL,
    reserved_until = NULL,
    booking_id = NULL
WHERE event_id IN (SELECT event_id FROM events WHERE event_name LIKE 'Load Test Event%');

-- Clean up test bookings
DELETE FROM booking_seats WHERE booking_id IN (
    SELECT booking_id FROM bookings
    WHERE user_id SIMILAR TO '(user|race-user|overlap-user|stress-user)-%'
);

DELETE FROM bookings
WHERE user_id SIMILAR TO '(user|race-user|overlap-user|stress-user)-%';

DELETE FROM reservations
WHERE user_id SIMILAR TO '(user|race-user|overlap-user|stress-user)-%';

-- Show reset results
SELECT
    COUNT(CASE WHEN status = 'AVAILABLE' THEN 1 END) as available_seats,
    COUNT(CASE WHEN status = 'RESERVED' THEN 1 END) as reserved_seats,
    COUNT(CASE WHEN status = 'BOOKED' THEN 1 END) as booked_seats
FROM seats
WHERE event_id IN (SELECT event_id FROM events WHERE event_name LIKE 'Load Test Event%');
SQL
EOF

chmod +x reset-test-data.sh
```

Then run between tests:
```bash
./reset-test-data.sh
```

## Test Sequence

Recommended order for running tests:

1. **First Run**: Booking Flow (baseline)
   ```bash
   ./seed-test-data.sh
   ./run-gatling-booking.sh -u 20 -d 30
   ```

2. **Reset and Scale Up**: More users
   ```bash
   ./reset-test-data.sh
   ./run-gatling-booking.sh -u 50 -d 60
   ```

3. **Race Condition Test**:
   ```bash
   ./reset-test-data.sh
   ./run-gatling-race.sh -u 100
   ```

4. **Stress Test** (optional):
   ```bash
   ./reset-test-data.sh
   ./run-gatling-stress.sh
   ```

## Understanding Results

### Good Signs ✅
- High success rate on reservations (>80%)
- 409 conflicts in race condition tests (shows proper locking)
- Predictable response times under normal load
- Graceful degradation under stress

### Warning Signs ⚠️
- Consistently high failure rates (>50%) on booking flow
- 404 errors (missing data)
- "Attribute not defined" errors (flow logic issues)
- Increasing response times that don't recover

### Critical Issues 🚨
- Double bookings (same seat booked by two users)
- Database connection pool exhaustion
- Memory leaks during sustained load
- Cascade failures that don't recover

## Next Steps

After successful load tests:
1. Analyze bottlenecks from reports
2. Monitor database query performance
3. Tune Redis connection pool
4. Adjust reservation timeout settings
5. Scale horizontally and re-test

## Additional Resources

- [Full Gatling Testing Guide](./GATLING_TESTING.md)
- [Quick Start Guide](./QUICKSTART_GATLING.md)
- [Gatling Documentation](https://gatling.io/docs/)
