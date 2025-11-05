# Ticket Booking System

A high-performance, scalable ticket booking system built with Java 21 and Quarkus 3.29.0 that handles race conditions, prevents double-booking, and supports flash sales with distributed locking.

## Features

### Core Capabilities
- **Distributed Locking**: Redis-based distributed locks to prevent race conditions
- **Two-Phase Booking**: Reservation → Payment → Confirmation flow
- **Automatic Cleanup**: Scheduled jobs to release expired reservations
- **Optimistic & Pessimistic Locking**: Support for both locking strategies
- **High Concurrency**: Handles thousands of concurrent booking requests
- **RESTful APIs**: Complete REST API for all operations

### Technical Highlights
- Java 21 with modern features
- Quarkus 3.29.0 for cloud-native performance
- PostgreSQL for persistent storage
- Redis for distributed locking
- Hibernate ORM with Panache
- Transaction management with retry logic
- Comprehensive error handling

## Architecture

### Components
1. **Entity Layer**: JPA entities for Event, Seat, Booking, Reservation
2. **Repository Layer**: Panache repositories for database operations
3. **Service Layer**: Business logic with distributed locking
4. **Controller Layer**: REST API endpoints
5. **Scheduler**: Background jobs for cleanup

### Database Schema
```
events          -> Event metadata and availability
seats           -> Seat inventory with status tracking
bookings        -> Confirmed bookings
booking_seats   -> Junction table for booking-seat relationship
reservations    -> Temporary seat holds
```

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose (for PostgreSQL and Redis)
- 4GB RAM minimum

## Quick Start

### 1. Start Infrastructure Services

Start PostgreSQL and Redis using Docker:

```bash
# PostgreSQL
docker run -d \
  --name postgres-ticket \
  -e POSTGRES_DB=ticketdb \
  -e POSTGRES_USER=ticketuser \
  -e POSTGRES_PASSWORD=ticketpass \
  -p 5432:5432 \
  postgres:16-alpine

# Redis
docker run -d \
  --name redis-ticket \
  -p 6379:6379 \
  redis:7-alpine
```

Or use Docker Compose (create docker-compose.yml):

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    container_name: postgres-ticket
    environment:
      POSTGRES_DB: ticketdb
      POSTGRES_USER: ticketuser
      POSTGRES_PASSWORD: ticketpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: redis-ticket
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

Then run:
```bash
docker-compose up -d
```

### 2. Build and Run Application

```bash
# Build the project
mvn clean package

# Run in development mode (with hot reload)
mvn quarkus:dev

# Or run the packaged application
java -jar target/quarkus-app/quarkus-run.jar
```

The application will start on `http://localhost:8080`

### 3. Verify Setup

```bash
# Health check
curl http://localhost:8080/api/bookings/health

# Get all events
curl http://localhost:8080/api/events

# Get available seats for an event
curl http://localhost:8080/api/events/1/seats/available
```

## API Documentation

### Event APIs

#### Get All Events
```bash
GET /api/events
```

#### Get Event by ID
```bash
GET /api/events/{eventId}
```

#### Get Upcoming Events
```bash
GET /api/events/upcoming
```

#### Get Events On Sale
```bash
GET /api/events/on-sale
```

#### Get Available Seats
```bash
GET /api/events/{eventId}/seats/available
```

### Booking APIs

#### Reserve Seats
```bash
POST /api/bookings/reserve
Content-Type: application/json

{
  "eventId": 1,
  "seatNumbers": ["A1", "A2"],
  "userId": "user123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Seats reserved successfully",
  "data": {
    "reservationId": 1,
    "seats": [
      {
        "seatId": 1,
        "seatNumber": "A1",
        "section": "VIP",
        "price": 299.99,
        "status": "RESERVED"
      }
    ],
    "expiresAt": "2025-11-05T14:30:00",
    "totalAmount": 599.98,
    "status": "ACTIVE"
  }
}
```

#### Confirm Booking
```bash
POST /api/bookings/confirm
Content-Type: application/json

{
  "reservationId": 1,
  "userId": "user123",
  "paymentRequest": {
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "amount": 599.98,
    "cvv": "123",
    "expiryDate": "12/26"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Booking confirmed successfully",
  "data": {
    "bookingId": 1,
    "bookingReference": "BK-1730820000000-ABC12345",
    "eventId": 1,
    "eventName": "Taylor Swift - Eras Tour",
    "totalAmount": 599.98,
    "status": "CONFIRMED",
    "paymentStatus": "SUCCESS",
    "confirmedAt": "2025-11-05T14:25:00"
  }
}
```

#### Get User Bookings
```bash
GET /api/bookings/user/{userId}
```

#### Get Booking by Reference
```bash
GET /api/bookings/reference/{bookingReference}
```

## Testing the System

### Test 1: Simple Booking Flow

```bash
# 1. Get available seats
curl http://localhost:8080/api/events/1/seats/available

# 2. Reserve seats
curl -X POST http://localhost:8080/api/bookings/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatNumbers": ["A1", "A2"],
    "userId": "user123"
  }'

# 3. Confirm booking (use reservationId from step 2)
curl -X POST http://localhost:8080/api/bookings/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": 1,
    "userId": "user123",
    "paymentRequest": {
      "paymentMethod": "CREDIT_CARD",
      "cardNumber": "4111111111111111",
      "cardHolderName": "John Doe",
      "amount": 599.98,
      "cvv": "123",
      "expiryDate": "12/26"
    }
  }'

# 4. View your bookings
curl http://localhost:8080/api/bookings/user/user123
```

### Test 2: Race Condition (Concurrent Bookings)

Use a tool like Apache JMeter or create a simple script:

```bash
# Create test script
cat > test-concurrent.sh << 'EOF'
#!/bin/bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/bookings/reserve \
    -H "Content-Type: application/json" \
    -d "{
      \"eventId\": 1,
      \"seatNumbers\": [\"B1\"],
      \"userId\": \"user${i}\"
    }" &
done
wait
EOF

chmod +x test-concurrent.sh
./test-concurrent.sh
```

**Expected Result**: Only ONE user should successfully reserve seat B1, others should get "Seat not available" error.

### Test 3: Reservation Expiry

```bash
# 1. Reserve a seat
curl -X POST http://localhost:8080/api/bookings/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatNumbers": ["C1"],
    "userId": "user999"
  }'

# 2. Wait for 10+ minutes (or change configuration to 1 minute)

# 3. Try to confirm after expiry - should fail
curl -X POST http://localhost:8080/api/bookings/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": <ID>,
    "userId": "user999",
    "paymentRequest": {...}
  }'

# Expected: "Reservation expired" error
```

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
ticketing:
  reservation:
    timeout-minutes: 10  # How long seats are reserved
  lock:
    timeout-seconds: 30   # Lock expiration time
  scheduler:
    cleanup:
      enabled: true
      interval: 60s       # Cleanup job frequency
```

### Database Configuration

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: ticketuser
    password: ticketpass
    jdbc:
      url: jdbc:postgresql://localhost:5432/ticketdb
```

### Redis Configuration

```yaml
quarkus:
  redis:
    hosts: redis://localhost:6379
    timeout: 10s
```

## Performance Tuning

### Database Connection Pool
```yaml
quarkus:
  datasource:
    jdbc:
      max-size: 20      # Increase for high concurrency
      min-size: 5
```

### JVM Options
```bash
java -Xmx2g -Xms1g -XX:+UseG1GC -jar target/quarkus-app/quarkus-run.jar
```

## Monitoring

### Application Logs
```bash
# View logs in dev mode
mvn quarkus:dev

# View logs for packaged app
java -jar target/quarkus-app/quarkus-run.jar | tee app.log
```

### Redis Monitor
```bash
# Connect to Redis CLI
docker exec -it redis-ticket redis-cli

# Monitor commands
MONITOR

# Check active locks
KEYS seat:lock:*

# Get lock value
GET seat:lock:1:A1
```

### Database Queries
```sql
-- Check reservation status
SELECT * FROM reservations WHERE status = 'ACTIVE';

-- Check seat availability
SELECT event_id, status, COUNT(*) 
FROM seats 
GROUP BY event_id, status;

-- Recent bookings
SELECT * FROM bookings ORDER BY created_at DESC LIMIT 10;
```

## Load Testing

### Using Apache Bench (ab)
```bash
# Test reservation endpoint
ab -n 1000 -c 50 -T application/json -p payload.json \
  http://localhost:8080/api/bookings/reserve
```

### Using JMeter
1. Create Thread Group with 1000 threads
2. Add HTTP Request for `/api/bookings/reserve`
3. Add JSON body with seat reservation
4. Run and analyze results

**Expected Performance**:
- 500+ reservations/second on modest hardware
- < 500ms response time for seat availability
- < 2s for complete booking flow

## Troubleshooting

### Issue: "Connection refused" to PostgreSQL
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres-ticket

# Verify connection
psql -h localhost -U ticketuser -d ticketdb
```

### Issue: Redis connection failed
```bash
# Check Redis
docker ps | grep redis

# Test Redis connection
docker exec -it redis-ticket redis-cli PING
# Should return: PONG
```

### Issue: Locks not releasing
```bash
# Connect to Redis
docker exec -it redis-ticket redis-cli

# Delete all locks (development only!)
FLUSHDB

# Or delete specific lock
DEL seat:lock:1:A1
```

### Issue: Build failures
```bash
# Clean build
mvn clean

# Skip tests
mvn clean package -DskipTests

# Update dependencies
mvn clean install -U
```

## Production Deployment

### Build Native Image (Optional)
```bash
# Requires GraalVM
mvn package -Dnative

# Run native executable
./target/ticket-booking-system-1.0.0-SNAPSHOT-runner
```

### Docker Deployment
```bash
# Build Docker image
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t ticket-booking-system .

# Run
docker run -p 8080:8080 ticket-booking-system
```

### Production Checklist
- [ ] Configure proper database credentials
- [ ] Set up Redis cluster for high availability
- [ ] Enable SSL/TLS for APIs
- [ ] Configure proper logging (JSON format)
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure rate limiting
- [ ] Set up load balancer
- [ ] Database backups
- [ ] Redis persistence configuration

## Architecture Decisions

### Why Redis for Distributed Locking?
- Fast in-memory operations
- Atomic operations with Lua scripts
- TTL support for automatic lock expiration
- Wide adoption and battle-tested

### Why Two-Phase Booking?
- Prevents race conditions
- User-friendly (reserves seats during payment)
- Handles payment failures gracefully
- Industry standard pattern

### Why Pessimistic + Optimistic Locking?
- Pessimistic: Critical sections (seat reservation)
- Optimistic: Event seat count updates (retry logic)
- Balance between consistency and performance

## Future Enhancements

- [ ] Add Redlock algorithm for Redis cluster
- [ ] Implement idempotency keys
- [ ] Add WebSocket for real-time seat updates
- [ ] Implement seat selection with preference (aisle, front row)
- [ ] Add dynamic pricing
- [ ] Implement waiting queue for sold-out events
- [ ] Add event search with Elasticsearch
- [ ] Implement JWT authentication
- [ ] Add rate limiting per user
- [ ] Implement saga pattern for distributed transactions
- [ ] Add metrics and monitoring
- [ ] Implement circuit breaker pattern

## License

MIT License - See LICENSE file for details

## Support

For issues and questions:
- GitHub Issues: Create an issue on the repository
- Documentation: See `/docs` folder for detailed documentation

## Contributors

Built with ❤️ following the design principles from the comprehensive ticket booking system design document.
