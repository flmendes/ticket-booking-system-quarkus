# Gatling Load Testing Guide

This project includes comprehensive Gatling load tests to verify the ticket booking system's performance and concurrency handling under various load conditions.

## Prerequisites

- Java 21
- Maven 3.x
- Running instance of the application (default: http://localhost:8080)
- Docker (for PostgreSQL and Redis if running locally)

## Test Simulations

### 1. BookingSimulation - Realistic User Behavior

Tests the complete booking flow with realistic user patterns.

**Features:**
- 70% users perform complete booking flow (reserve + confirm)
- 25% users check their bookings
- 5% health check requests
- Configurable concurrent users, ramp-up time, and duration

**Usage:**
```bash
# Default settings (50 users, 10s ramp, 60s duration)
./run-gatling-booking.sh

# Custom configuration
./run-gatling-booking.sh -u 100 -r 20 -d 120

# With custom base URL
./run-gatling-booking.sh -b http://production-server:8080 -u 200
```

**Parameters:**
- `-u, --users`: Number of concurrent users (default: 50)
- `-r, --ramp`: Ramp-up duration in seconds (default: 10)
- `-d, --duration`: Test duration in seconds (default: 60)
- `-b, --base-url`: Base URL of the application (default: http://localhost:8080)

**Expected Results:**
- Max response time: < 5000ms
- Success rate: > 90%
- 99.9th percentile: < 2000ms

---

### 2. RaceConditionSimulation - Concurrency Testing

Tests race conditions with multiple users competing for the same seats.

**Features:**
- Test 1: 50 users attempt to book the exact same seat simultaneously
- Test 2: 100 users compete for 10 available seats
- Test 3: Burst load with 200 users hitting at once
- Validates proper handling of conflicts (409 status codes)

**Usage:**
```bash
# Default settings (100 users, seat A1, event 1)
./run-gatling-race.sh

# Test specific seat and event
./run-gatling-race.sh -s "VIP-5" -e 2 -u 150

# High concurrency test
./run-gatling-race.sh -u 300
```

**Parameters:**
- `-u, --users`: Number of concurrent users (default: 100)
- `-s, --seat`: Target seat number (default: A1)
- `-e, --event-id`: Event ID to test (default: 1)
- `-b, --base-url`: Base URL of the application (default: http://localhost:8080)

**Expected Results:**
- Failed requests: < 80% (conflicts are expected and acceptable)
- Max response time: < 10000ms
- 99.9th percentile: < 5000ms
- Exactly one user should successfully book each contested seat

---

### 3. StressTestSimulation - Progressive Load Testing

Tests system behavior under extreme load with progressive stress increase.

**Features:**
- Phase 1: Baseline (normal load for 30s)
- Phase 2: Gradual ramp-up (60s)
- Phase 3: Sustained stress (120s)
- Phase 4: Spike load (30s at peak)
- Phase 5: Recovery phase (60s ramp-down)
- Continuous read operations throughout
- Tests multiple events and variable seat counts

**Usage:**
```bash
# Default settings (10 -> 100 -> 500 users/sec)
./run-gatling-stress.sh

# Custom load levels
./run-gatling-stress.sh -n 5 -s 50 -p 200

# Extreme stress test
./run-gatling-stress.sh -n 20 -s 200 -p 1000
```

**Parameters:**
- `-n, --normal`: Normal load in users/sec (default: 10)
- `-s, --stress`: Stress load in users/sec (default: 100)
- `-p, --spike`: Spike load in users/sec (default: 500)
- `-b, --base-url`: Base URL of the application (default: http://localhost:8080)

**Expected Results:**
- Failed requests: < 30% (under stress, some failures are acceptable)
- 99.99th percentile: < 30000ms
- System should recover gracefully during ramp-down phase
- At least 100 successful requests

---

## Running Tests Manually with Maven

You can also run tests directly using Maven:

```bash
# Run specific simulation
./mvnw gatling:test -Dgatling.simulationClass=com.ticketing.gatling.BookingSimulation

# With custom parameters
./mvnw gatling:test \
  -Dgatling.simulationClass=com.ticketing.gatling.RaceConditionSimulation \
  -DbaseUrl=http://localhost:8080 \
  -Dusers=200 \
  -DtargetSeat=A5

# Run all simulations
./mvnw gatling:test
```

## Viewing Reports

After each test run, Gatling generates an HTML report with detailed metrics:

```bash
# Reports are located in
target/gatling/

# Open the latest report (macOS)
open target/gatling/$(ls -t target/gatling/ | head -1)/index.html

# Or on Linux
xdg-open target/gatling/$(ls -t target/gatling/ | head -1)/index.html
```

The report includes:
- Request/response time distributions
- Success/failure rates
- Active users over time
- Response time percentiles
- Detailed metrics per request

## Test Scenarios Explained

### BookingSimulation Use Cases
- **E-commerce flash sales**: Simulate high-demand ticket releases
- **Load capacity planning**: Determine max concurrent users
- **Performance regression testing**: Compare against previous runs
- **SLA validation**: Verify response time requirements

### RaceConditionSimulation Use Cases
- **Distributed lock testing**: Verify Redis-based locking works correctly
- **Database transaction isolation**: Test pessimistic locking
- **Conflict resolution**: Ensure proper 409 responses
- **Data consistency**: Verify no double-bookings occur

### StressTestSimulation Use Cases
- **Breaking point analysis**: Find system limits
- **Auto-scaling verification**: Test Kubernetes HPA triggers
- **Resource bottleneck identification**: Find CPU/memory/DB limits
- **Recovery testing**: Verify system recovers from overload

## Best Practices

1. **Start with lower loads**: Begin with default settings and gradually increase
2. **Monitor system resources**: Watch CPU, memory, database connections during tests
3. **Check logs**: Review application logs for errors during high load
4. **Database state**: Reset test data between runs for consistent results
5. **Network conditions**: Run from same network as production for accurate results
6. **Warm-up**: Do a small warm-up run before major tests

## Troubleshooting

### Connection Refused Errors
- Ensure application is running: `curl http://localhost:8080/api/bookings/health`
- Check application logs for startup errors
- Verify PostgreSQL and Redis are running

### Too Many Open Files
Increase file descriptor limits:
```bash
ulimit -n 65536
```

### Out of Memory
Increase JVM heap for Maven:
```bash
export MAVEN_OPTS="-Xmx2g"
```

### High Failure Rates
- Check application logs for errors
- Verify database connection pool size
- Monitor Redis connection limits
- Review timeout configurations

## Integration with CI/CD

Example GitLab CI configuration:
```yaml
performance-test:
  stage: test
  script:
    - ./run-gatling-booking.sh -u 100 -d 300
    - ./run-gatling-race.sh -u 200
  artifacts:
    paths:
      - target/gatling/
    when: always
  only:
    - master
    - performance
```

## Metrics to Monitor

During load tests, monitor:
- **Application**: Response times, error rates, throughput
- **Database**: Connection pool usage, query performance, locks
- **Redis**: Connection count, memory usage, keyspace hits/misses
- **Infrastructure**: CPU, memory, network I/O, disk I/O

## Performance Baselines

Expected performance on standard hardware (4 CPU, 8GB RAM):
- **Normal load** (50 users): < 200ms avg response time
- **Stress load** (100 users): < 500ms avg response time
- **Spike load** (200+ users): < 2000ms avg response time
- **Throughput**: > 100 requests/sec sustained

Your results may vary based on:
- Hardware specifications
- Database configuration
- Network latency
- Application configuration
- Redis/PostgreSQL tuning
