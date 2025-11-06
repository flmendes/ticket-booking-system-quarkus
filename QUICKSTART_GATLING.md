# Gatling Quick Start Guide

## Setup Complete!

Your Gatling load testing infrastructure is now ready. Here's what was created:

### Test Simulations

1. **BookingSimulation.java** - Realistic user behavior testing
2. **RaceConditionSimulation.java** - Concurrency and race condition testing
3. **StressTestSimulation.java** - Progressive load and stress testing

### Helper Scripts

- `seed-test-data.sh` - Create test events and seats in database
- `reset-test-data.sh` - Reset test data between runs
- `run-gatling-booking.sh` - Run booking flow tests
- `run-gatling-race.sh` - Run race condition tests
- `run-gatling-stress.sh` - Run stress tests

## Quick Start

### 1. Start the Application

Make sure your application is running:

```bash
# Start PostgreSQL and Redis (if using Docker)
docker-compose up -d

# Start the Quarkus application in dev mode
./mvnw quarkus:dev
```

### 2. Seed Test Data (IMPORTANT - First Time Only)

Create test events and seats in the database:

```bash
./seed-test-data.sh
```

This creates:
- 5 test events (Load Test Event 1-5)
- 2000+ seats with various types (VIP, PREMIUM, REGULAR)
- All in AVAILABLE status ready for testing

**⚠️ Skip this step and you'll get 404 errors!**

**Note**: The script uses `docker exec` to run commands in the PostgreSQL container started by docker-compose. Make sure the containers are running first with `docker-compose up -d`.

### 3. Run Your First Test

```bash
# Simple booking flow test with 50 concurrent users
./run-gatling-booking.sh

# Or with custom settings
./run-gatling-booking.sh -u 100 -d 120
```

### 3. View the Report

After the test completes, open the generated HTML report:

```bash
# The test will show the report location, e.g.:
# target/gatling/bookingsimulation-20250105175800/index.html

# On macOS
open target/gatling/$(ls -t target/gatling/ | head -1)/index.html
```

## Test Scenarios

### Booking Flow Test (Recommended Start)
```bash
./run-gatling-booking.sh -u 50 -r 10 -d 60
```
- Tests complete reservation + payment flow
- Good for initial performance baseline
- Expected: >90% success rate, <2s response time

### Race Condition Test
```bash
./run-gatling-race.sh -u 100 -s "A1" -e 1
```
- Tests multiple users booking same seats
- Validates distributed locking
- Expected: Proper conflict handling (409 status)

### Stress Test (Advanced)
```bash
./run-gatling-stress.sh -n 10 -s 100 -p 500
```
- Progressive load increase
- Tests system limits
- Expected: Graceful degradation under load

## Understanding Results

### Key Metrics to Watch

1. **Response Time Percentiles**
   - P50 (median): Should be < 200ms
   - P95: Should be < 1000ms
   - P99: Should be < 2000ms

2. **Success Rate**
   - Normal load: > 95%
   - Stress load: > 70%
   - Race conditions: Conflicts are expected

3. **Requests per Second**
   - Throughput indicator
   - Should scale with user count

### Common Scenarios

**Scenario: All requests failing**
- Check if application is running
- Verify database connections
- Check application logs

**Scenario: Slow response times**
- Check database query performance
- Monitor Redis connections
- Review application CPU/memory

**Scenario: Many 409 conflicts**
- This is expected in race condition tests
- Validates proper seat locking
- Check that only one booking succeeds per seat

## Next Steps

1. **Establish Baseline**: Run booking test with default settings
2. **Test Concurrency**: Run race condition test
3. **Find Limits**: Gradually increase load in stress test
4. **Monitor Resources**: Watch CPU, memory, database during tests
5. **Optimize**: Based on bottlenecks found

## Tips

- Start small and increase load gradually
- **Reset test data between runs** for consistency: `./reset-test-data.sh`
- Monitor application logs during tests
- Use the reports to identify bottlenecks
- Run tests from same network as deployment for accuracy

## Resetting Data Between Tests

After running tests, seats get booked. Reset them before the next run:

```bash
./reset-test-data.sh
```

This resets all test seats to AVAILABLE and removes test bookings.

## Troubleshooting

### Getting 404 or "No attribute 'reservationId'" errors?
**Solution**: You need to seed test data first!
```bash
./seed-test-data.sh
```

### All seats already booked?
**Solution**: Reset the test data
```bash
./reset-test-data.sh
```

### Application not responding?
**Solution**: Make sure it's running
```bash
curl http://localhost:8080/api/bookings/health
```

## Need More Help?

- **Docker setup?** See: [DOCKER_SCRIPTS_USAGE.md](./DOCKER_SCRIPTS_USAGE.md)
- **Setup issues?** See: [LOAD_TEST_SETUP.md](./LOAD_TEST_SETUP.md)
- **Full documentation**: [GATLING_TESTING.md](./GATLING_TESTING.md)
