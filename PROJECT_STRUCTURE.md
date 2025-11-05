# Project Structure

## Overview
```
ticket-booking-system/
├── pom.xml                          # Maven project configuration
├── docker-compose.yml               # Infrastructure services
├── README.md                        # Main documentation
├── PROJECT_STRUCTURE.md            # This file
├── .gitignore                      # Git ignore rules
├── test-booking-flow.sh            # End-to-end test script
├── test-race-condition.sh          # Concurrent booking test
└── src/
    ├── main/
    │   ├── java/com/ticketing/
    │   │   ├── entity/              # JPA Entity classes
    │   │   │   ├── Event.java       # Event entity
    │   │   │   ├── Seat.java        # Seat entity
    │   │   │   ├── Booking.java     # Booking entity
    │   │   │   ├── BookingSeat.java # Booking-Seat junction
    │   │   │   └── Reservation.java # Reservation entity
    │   │   │
    │   │   ├── repository/          # Data access layer
    │   │   │   ├── EventRepository.java
    │   │   │   ├── SeatRepository.java
    │   │   │   └── Repositories.java # Booking, BookingSeat, Reservation repos
    │   │   │
    │   │   ├── service/             # Business logic layer
    │   │   │   ├── DistributedLockService.java    # Redis-based locking
    │   │   │   ├── TicketBookingService.java      # Main booking logic
    │   │   │   ├── PaymentService.java            # Payment processing
    │   │   │   ├── ReservationCleanupService.java # Scheduled cleanup
    │   │   │   └── DataInitializationService.java # Sample data
    │   │   │
    │   │   ├── controller/          # REST API layer
    │   │   │   ├── BookingController.java  # Booking endpoints
    │   │   │   └── EventController.java    # Event & seat endpoints
    │   │   │
    │   │   ├── dto/                 # Data Transfer Objects
    │   │   │   └── DTOs.java        # All request/response DTOs
    │   │   │
    │   │   └── exception/           # Custom exceptions
    │   │       └── Exceptions.java  # All exception classes
    │   │
    │   └── resources/
    │       └── application.yml      # Application configuration
    │
    └── test/
        └── java/com/ticketing/      # Test classes (to be added)
```

## Layer Descriptions

### 1. Entity Layer (`com.ticketing.entity`)
**Purpose**: JPA entities representing database tables

**Files**:
- `Event.java` - Concert/show/movie event with metadata
- `Seat.java` - Individual seat with status and pricing
- `Booking.java` - Confirmed booking after payment
- `BookingSeat.java` - Many-to-many relationship between bookings and seats
- `Reservation.java` - Temporary seat hold with expiration

**Key Features**:
- Optimistic locking with `@Version`
- Proper indexes for query performance
- Enum types for status fields
- Lifecycle callbacks (`@PrePersist`)

### 2. Repository Layer (`com.ticketing.repository`)
**Purpose**: Data access with Panache repositories

**Files**:
- `EventRepository.java` - Event queries and updates
- `SeatRepository.java` - Seat availability and locking queries
- `Repositories.java` - Booking, BookingSeat, Reservation repositories

**Key Features**:
- Custom queries with JPQL
- Pessimistic locking support
- Optimistic locking with version checks
- Bulk operations for efficiency

### 3. Service Layer (`com.ticketing.service`)
**Purpose**: Business logic and transaction management

**Files**:
- `DistributedLockService.java`
  - Redis-based distributed locks
  - Lua script for atomic operations
  - Multi-resource locking with deadlock prevention
  
- `TicketBookingService.java`
  - Complete booking workflow
  - Reservation → Payment → Confirmation
  - Automatic rollback on failures
  
- `PaymentService.java`
  - Mock payment processing
  - Refund support (compensating transactions)
  
- `ReservationCleanupService.java`
  - Scheduled job for expired reservations
  - Statistics logging
  
- `DataInitializationService.java`
  - Sample data creation on startup
  - Multiple events with seats

**Key Features**:
- `@Transactional` for ACID guarantees
- Distributed locking for race condition prevention
- Retry logic for optimistic locking
- Comprehensive error handling

### 4. Controller Layer (`com.ticketing.controller`)
**Purpose**: REST API endpoints

**Files**:
- `BookingController.java`
  - POST /api/bookings/reserve - Reserve seats
  - POST /api/bookings/confirm - Confirm booking
  - GET /api/bookings/user/{userId} - User bookings
  - GET /api/bookings/reference/{ref} - Get by reference
  
- `EventController.java`
  - GET /api/events - All events
  - GET /api/events/{id} - Event details
  - GET /api/events/upcoming - Upcoming events
  - GET /api/events/{id}/seats/available - Available seats

**Key Features**:
- JAX-RS annotations
- JSON request/response
- Comprehensive error handling
- HTTP status codes

### 5. DTO Layer (`com.ticketing.dto`)
**Purpose**: Data transfer objects for API

**Classes** (all in `DTOs.java`):
- Request DTOs:
  - `ReservationRequest`
  - `PaymentRequest`
  - `BookingConfirmRequest`
  
- Response DTOs:
  - `ReservationResponse`
  - `BookingResponse`
  - `EventResponse`
  - `SeatInfo`
  - `PaymentResponse`
  - `ApiResponse<T>` (generic wrapper)

**Key Features**:
- Bean validation annotations
- Builder pattern with Lombok
- Type-safe data transfer

### 6. Exception Layer (`com.ticketing.exception`)
**Purpose**: Custom business exceptions

**Classes** (all in `Exceptions.java`):
- `SeatNotFoundException`
- `SeatNotAvailableException`
- `ReservationNotFoundException`
- `ReservationExpiredException`
- `PaymentFailedException`
- `InvalidSeatStateException`
- `BookingFailureException`

## Design Patterns Used

### 1. Repository Pattern
- Separates data access from business logic
- Panache provides ActiveRecord + Repository patterns

### 2. Service Layer Pattern
- Business logic isolated from controllers
- Reusable across different interfaces

### 3. DTO Pattern
- API contracts separate from domain models
- Version API independently

### 4. Builder Pattern
- Lombok `@Builder` for clean object construction
- Fluent API for complex objects

### 5. Transaction Script Pattern
- Each service method is a transaction
- Clear transaction boundaries

### 6. Distributed Lock Pattern
- Redis-based coordination
- Prevents race conditions across multiple instances

### 7. Two-Phase Commit (Simplified)
- Reservation phase (temporary)
- Confirmation phase (permanent)
- Automatic cleanup on timeout

## Database Schema

```sql
-- Events (metadata)
events (event_id, event_name, event_date, venue_name, 
        total_seats, available_seats, status, sale_start_time, 
        version, created_at)

-- Seats (inventory)
seats (seat_id, event_id, seat_number, section, row_number,
       seat_type, price, status, version, reserved_by, 
       reserved_until, booking_id, created_at)

-- Bookings (confirmed orders)
bookings (booking_id, event_id, user_id, total_amount, 
          status, payment_id, payment_status, 
          booking_reference, created_at, confirmed_at)

-- Booking-Seat junction
booking_seats (booking_seat_id, booking_id, seat_id, price)

-- Reservations (temporary holds)
reservations (reservation_id, seat_id, event_id, user_id, 
              session_id, expires_at, status, created_at)
```

## Key Technologies

### Core Framework
- **Quarkus 3.29.0** - Kubernetes-native Java framework
- **Java 21** - Latest LTS with modern features

### Persistence
- **Hibernate ORM** - JPA implementation
- **Panache** - Simplified persistence layer
- **PostgreSQL** - Relational database

### Caching & Locking
- **Redis** - Distributed locks and caching

### Validation
- **Hibernate Validator** - Bean validation

### Scheduling
- **Quarkus Scheduler** - CRON-based jobs

### Logging
- **JBoss Logging** - Unified logging facade

## Configuration Management

### Application Properties
Located in `src/main/resources/application.yml`

**Sections**:
1. Database configuration
2. Redis configuration
3. HTTP/CORS settings
4. Custom application properties
5. Logging configuration

### Environment-Specific Config
- Development: `application.yml`
- Production: Override with environment variables or `application-prod.yml`

## Testing Strategy

### Test Scripts
- `test-booking-flow.sh` - Complete user journey
- `test-race-condition.sh` - Concurrent booking stress test

### Manual Testing
1. Start infrastructure: `docker-compose up`
2. Run application: `mvn quarkus:dev`
3. Execute test scripts
4. Monitor logs and Redis

### Load Testing
- Apache Bench (ab)
- JMeter test plans
- Gatling scenarios

## Deployment Options

### Development
```bash
mvn quarkus:dev
```

### Packaged JAR
```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Executable
```bash
mvn package -Dnative
./target/*-runner
```

### Docker
```bash
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t ticket-system .
docker run -p 8080:8080 ticket-system
```

## Monitoring & Debugging

### Application Logs
- Debug level for `com.ticketing` package
- INFO level for framework
- Structured logging with context

### Redis Monitoring
```bash
docker exec -it redis-ticket redis-cli MONITOR
```

### Database Queries
```sql
-- Active reservations
SELECT * FROM reservations WHERE status = 'ACTIVE';

-- Seat distribution
SELECT status, COUNT(*) FROM seats GROUP BY status;

-- Recent bookings
SELECT * FROM bookings ORDER BY created_at DESC LIMIT 10;
```

### Health Checks
- Application: `GET /api/bookings/health`
- PostgreSQL: `docker ps` + connection test
- Redis: `redis-cli PING`

## Performance Characteristics

### Expected Throughput
- 500+ seat reservations/second
- < 500ms seat availability check
- < 2s complete booking flow
- 20+ database connections

### Scalability
- Horizontal scaling supported (via Redis locks)
- Database connection pooling
- Stateless application design
- Redis cluster for HA

## Security Considerations

### Current State (Development)
- No authentication/authorization
- Open CORS policy
- Mock payment processing

### Production Requirements
- JWT-based authentication
- Role-based access control (RBAC)
- Rate limiting per user
- Input validation and sanitization
- SQL injection prevention (Hibernate)
- HTTPS/TLS encryption
- Payment gateway integration
- PCI DSS compliance for payments

## Future Enhancements

### High Priority
1. JWT authentication implementation
2. Real payment gateway integration
3. Idempotency keys for API calls
4. WebSocket for real-time updates
5. Event search with Elasticsearch

### Medium Priority
6. Redlock algorithm for Redis cluster
7. Circuit breaker pattern
8. Saga pattern for distributed transactions
9. Metrics and monitoring (Prometheus)
10. API rate limiting

### Low Priority
11. GraphQL API
12. Admin dashboard
13. Email notifications
14. SMS notifications
15. Recommendation engine

## Contributing Guidelines

### Code Style
- Follow Java conventions
- Use Lombok judiciously
- Add JavaDoc for public APIs
- Write meaningful commit messages

### Pull Request Process
1. Create feature branch
2. Implement changes with tests
3. Update documentation
4. Submit PR with description
5. Address review comments

### Testing Requirements
- Unit tests for services
- Integration tests for APIs
- Load tests for critical paths
- Manual testing documented

## Support & Resources

### Documentation
- README.md - Getting started
- PROJECT_STRUCTURE.md - This file
- Quarkus docs: https://quarkus.io/guides/

### Community
- GitHub Issues for bugs
- Discussions for questions
- Stack Overflow for general help

---

**Last Updated**: 2025-11-05  
**Version**: 1.0.0  
**Maintainer**: Development Team
