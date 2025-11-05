# Quick Start Guide

Get the ticket booking system running in 5 minutes!

## Prerequisites Check

```bash
# Check Java version (need 21+)
java -version

# Check Maven
mvn -version

# Check Docker
docker --version
docker-compose --version
```

## 1. Start Infrastructure (1 minute)

```bash
cd ticket-booking-system
docker-compose up -d
```

**Wait for services**:
```bash
# Check PostgreSQL
docker exec postgres-ticket pg_isready -U ticketuser

# Check Redis
docker exec redis-ticket redis-cli ping
```

## 2. Start Application (2 minutes)

```bash
# Development mode (with hot reload)
mvn quarkus:dev

# OR build and run
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

Application starts at: **http://localhost:8080**

## 3. Test the System (2 minutes)

### Quick Health Check
```bash
curl http://localhost:8080/api/bookings/health
```

### View Events
```bash
curl http://localhost:8080/api/events | jq
```

### Run Complete Test Suite
```bash
./test-booking-flow.sh
```

### Test Race Conditions
```bash
./test-race-condition.sh
```

## Common Issues

### PostgreSQL won't start
```bash
docker-compose down
docker volume rm ticket-booking-system_postgres_data
docker-compose up -d
```

### Redis connection error
```bash
docker restart redis-ticket
```

### Port already in use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.yml
quarkus:
  http:
    port: 8081
```

## API Endpoints

### Reserve Seats
```bash
curl -X POST http://localhost:8080/api/bookings/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatNumbers": ["A1", "A2"],
    "userId": "user123"
  }'
```

### Confirm Booking
```bash
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
```

### Get Your Bookings
```bash
curl http://localhost:8080/api/bookings/user/user123
```

## Next Steps

- Read [README.md](README.md) for detailed documentation
- See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for architecture
- Run load tests with JMeter
- Explore the code in your IDE

## Shutdown

```bash
# Stop application: Ctrl+C

# Stop infrastructure
docker-compose down

# Remove data (optional)
docker-compose down -v
```

---

**Need Help?** Check README.md or create an issue on GitHub.
