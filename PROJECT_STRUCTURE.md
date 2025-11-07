# Project Structure

## Overview
```
ticket-booking-system/
├── pom.xml                          # Maven project configuration
├── docker-compose.yml               # Infrastructure services
├── README.md                        # Main documentation
├── PROJECT_STRUCTURE.md             # This file
├── QUICKSTART.md                    # Quick start guide
├── IMPLEMENTATION_SUMMARY.md        # Implementation details
├── EXCEPTION_HANDLING.md            # Exception handling strategy
├── DATASOURCE_QUICKREF.md           # Database configuration reference
├── DATASOURCE_TUNING.md             # Database tuning guide
├── DOCKER_SCRIPTS_USAGE.md          # Docker scripts documentation
├── README_SCRIPTS.md                # Test scripts documentation
├── BUGFIX_VERSION_NULL.md           # Version null bugfix documentation
├── BUGFIX_PAYMENT_402.md            # Payment 402 error bugfix documentation
├── CHANGELOG_DATASOURCE.md          # Datasource changelog
├── INIT_DATA_SCRIPT.md              # Sample data script documentation
├── TEST_DATA_GUIDE.md               # Test data usage guide
├── GITHUB_ACTIONS.md                # CI/CD workflows documentation
├── SET_DEFAULT_BRANCH.md            # Guide to set main as default branch
├── .gitignore                       # Git ignore rules
├── .github/
│   ├── workflows/
│   │   ├── ci-develop.yml           # JVM image build for develop branch
│   │   ├── ci-main.yml              # Native image build for main branch
│   │   └── pr-validation.yml        # Quick PR validation
│   └── dependency-check-suppressions.xml  # OWASP suppressions
├── test-booking-flow.sh             # End-to-end test script
├── test-race-condition.sh           # Concurrent booking test
├── fix-database-versions.sh         # Database version fix script
├── init-sample-data.sh              # Sample data initialization script
└── src/
    ├── main/
    │   ├── java/com/ticketing/
    │   │   ├── booking/             # Booking domain module
    │   │   │   ├── api/
    │   │   │   │   └── BookingController.java      # Booking endpoints
    │   │   │   ├── domain/
    │   │   │   │   ├── Booking.java                # Booking entity
    │   │   │   │   └── BookingSeat.java            # Booking-Seat junction
    │   │   │   ├── dto/
    │   │   │   │   ├── BookingConfirmRequest.java
    │   │   │   │   ├── BookingResponse.java
    │   │   │   │   ├── PaymentRequest.java
    │   │   │   │   └── PaymentResponse.java
    │   │   │   ├── exception/
    │   │   │   │   ├── BookingFailureException.java
    │   │   │   │   ├── InvalidSeatStateException.java
    │   │   │   │   └── PaymentFailedException.java
    │   │   │   ├── repository/
    │   │   │   │   ├── BookingRepository.java
    │   │   │   │   └── BookingSeatRepository.java
    │   │   │   └── service/
    │   │   │       ├── BookingService.java         # Booking business logic
    │   │   │       └── PaymentService.java         # Payment processing
    │   │   │
    │   │   ├── event/               # Event domain module
    │   │   │   ├── api/
    │   │   │   │   └── EventController.java        # Event & seat endpoints
    │   │   │   ├── domain/
    │   │   │   │   ├── Event.java                  # Event entity
    │   │   │   │   └── Seat.java                   # Seat entity
    │   │   │   ├── dto/
    │   │   │   │   ├── EventResponse.java
    │   │   │   │   └── SeatInfo.java
    │   │   │   ├── exception/
    │   │   │   │   ├── EventNotFoundException.java
    │   │   │   │   ├── SeatNotAvailableException.java
    │   │   │   │   └── SeatNotFoundException.java
    │   │   │   └── repository/
    │   │   │       ├── EventRepository.java
    │   │   │       └── SeatRepository.java
    │   │   │
    │   │   ├── reservation/         # Reservation domain module
    │   │   │   ├── api/
    │   │   │   │   └── ReservationController.java  # Reservation endpoints
    │   │   │   ├── domain/
    │   │   │   │   └── Reservation.java            # Reservation entity
    │   │   │   ├── dto/
    │   │   │   │   ├── ReservationRequest.java
    │   │   │   │   └── ReservationResponse.java
    │   │   │   ├── exception/
    │   │   │   │   ├── InvalidReservationException.java
    │   │   │   │   ├── ReservationExpiredException.java
    │   │   │   │   └── ReservationNotFoundException.java
    │   │   │   ├── repository/
    │   │   │   │   └── ReservationRepository.java
    │   │   │   └── service/
    │   │   │       ├── ReservationService.java     # Reservation logic
    │   │   │       └── ReservationCleanupService.java # Scheduled cleanup
    │   │   │
    │   │   └── shared/              # Shared infrastructure module
    │   │       ├── dto/
    │   │       │   ├── ApiResponse.java            # Generic API response wrapper
    │   │       │   ├── ErrorResponse.java          # Error response format
    │   │       │   └── ProblemDetail.java          # RFC 7807 problem details
    │   │       ├── exception/
    │   │       │   ├── ConcurrentModificationException.java
    │   │       │   └── handler/                    # Global exception handlers
    │   │       │       ├── BookingFailureExceptionMapper.java
    │   │       │       ├── ConcurrentModificationExceptionMapper.java
    │   │       │       ├── EventNotFoundExceptionMapper.java
    │   │       │       ├── GenericExceptionMapper.java
    │   │       │       ├── InvalidReservationExceptionMapper.java
    │   │       │       ├── InvalidSeatStateExceptionMapper.java
    │   │       │       ├── NotFoundExceptionMapper.java
    │   │       │       ├── PaymentFailedExceptionMapper.java
    │   │       │       ├── ReservationExpiredExceptionMapper.java
    │   │       │       ├── ReservationNotFoundExceptionMapper.java
    │   │       │       ├── SeatNotAvailableExceptionMapper.java
    │   │       │       ├── SeatNotFoundExceptionMapper.java
    │   │       │       └── ValidationExceptionMapper.java
    │   │       └── service/
    │   │           ├── DistributedLockService.java # Redis-based locking
    │   │           └── DataInitializationService.java # Sample data
    │   │
    │   └── resources/
    │       ├── application.yml                      # Main configuration
    │       └── application-loadtest.yml             # Load test configuration
    │
    └── test/
        ├── java/com/ticketing/
        │   ├── TestDataHelper.java                  # Test data constants and utilities
        │   └── it/                                   # Integration tests
        │       ├── BookingFlowIT.java
        │       ├── BookingFlowTest.java
        │       ├── RaceConditionIT.java
        │       └── RaceConditionTest.java
        └── resources/
            ├── application.yml                       # Test configuration
            └── import.sql                            # Test data initialization
```

## Module Descriptions

The project follows a **Domain-Driven Design (DDD)** approach with modules organized by business domain. Each module contains its own API layer, domain entities, DTOs, exceptions, repositories, and services.

### 1. Booking Module (`com.ticketing.booking`)
**Purpose**: Manages confirmed bookings and payment processing

**Structure**:
- **api/** - REST endpoints for booking operations
  - `BookingController.java` - Booking confirmation endpoints

- **domain/** - Booking entities
  - `Booking.java` - Confirmed booking entity with payment details
  - `BookingSeat.java` - Junction table for booking-seat relationship

- **dto/** - Booking data transfer objects
  - `BookingConfirmRequest.java` - Booking confirmation request
  - `BookingResponse.java` - Booking details response
  - `PaymentRequest.java` - Payment processing request
  - `PaymentResponse.java` - Payment result response

- **exception/** - Booking-specific exceptions
  - `BookingFailureException.java` - General booking failures
  - `InvalidSeatStateException.java` - Invalid seat state errors
  - `PaymentFailedException.java` - Payment processing errors

- **repository/** - Data access layer
  - `BookingRepository.java` - Booking persistence
  - `BookingSeatRepository.java` - Booking-seat relationship persistence

- **service/** - Business logic
  - `BookingService.java` - Booking workflow and confirmation
  - `PaymentService.java` - Payment processing (mock implementation)

**Key Features**:
- Two-phase booking process (reserve → confirm)
- Payment integration with rollback support
- Booking reference generation
- Transaction management

### 2. Event Module (`com.ticketing.event`)
**Purpose**: Manages events and seat inventory

**Structure**:
- **api/** - REST endpoints for events
  - `EventController.java` - Event and seat query endpoints

- **domain/** - Event entities
  - `Event.java` - Event metadata (name, date, venue)
  - `Seat.java` - Seat inventory with status and pricing

- **dto/** - Event data transfer objects
  - `EventResponse.java` - Event details response
  - `SeatInfo.java` - Seat information

- **exception/** - Event-specific exceptions
  - `EventNotFoundException.java` - Event not found errors
  - `SeatNotAvailableException.java` - Seat unavailability errors
  - `SeatNotFoundException.java` - Seat not found errors

- **repository/** - Data access layer
  - `EventRepository.java` - Event queries and updates
  - `SeatRepository.java` - Seat availability and locking queries

**Key Features**:
- Event catalog management
- Real-time seat availability
- Optimistic locking with `@Version`
- Seat filtering by section, type, price

### 3. Reservation Module (`com.ticketing.reservation`)
**Purpose**: Manages temporary seat reservations with expiration

**Structure**:
- **api/** - REST endpoints for reservations
  - `ReservationController.java` - Reservation creation and management

- **domain/** - Reservation entities
  - `Reservation.java` - Temporary seat hold with expiration time

- **dto/** - Reservation data transfer objects
  - `ReservationRequest.java` - Reservation creation request
  - `ReservationResponse.java` - Reservation details response

- **exception/** - Reservation-specific exceptions
  - `InvalidReservationException.java` - Invalid reservation errors
  - `ReservationExpiredException.java` - Expired reservation errors
  - `ReservationNotFoundException.java` - Reservation not found errors

- **repository/** - Data access layer
  - `ReservationRepository.java` - Reservation persistence and queries

- **service/** - Business logic
  - `ReservationService.java` - Reservation creation and validation
  - `ReservationCleanupService.java` - Scheduled cleanup of expired reservations

**Key Features**:
- Temporary seat holds with TTL
- Automatic expiration and cleanup
- Session-based reservations
- Distributed locking for seat allocation

### 4. Shared Module (`com.ticketing.shared`)
**Purpose**: Cross-cutting concerns and infrastructure

**Structure**:
- **dto/** - Generic response wrappers
  - `ApiResponse.java` - Generic API response wrapper
  - `ErrorResponse.java` - Standardized error response
  - `ProblemDetail.java` - RFC 7807 problem details format

- **exception/** - Global exception handling
  - `ConcurrentModificationException.java` - Optimistic locking failures
  - **handler/** - JAX-RS exception mappers
    - 13 exception mappers for comprehensive error handling
    - Standardized HTTP status codes and error messages
    - Logging and error tracking

- **service/** - Shared infrastructure services
  - `DistributedLockService.java` - Redis-based distributed locking

**Key Features**:
- Centralized exception handling with JAX-RS ExceptionMappers
- RFC 7807 compliant error responses
- Distributed locking with Redis and Lua scripts
- Multi-resource locking with deadlock prevention
- Global error logging and monitoring

## API Endpoints

### Booking API (`/api/bookings`)
- `POST /api/bookings/confirm` - Confirm a booking with payment
- `GET /api/bookings/user/{userId}` - Get all bookings for a user
- `GET /api/bookings/reference/{reference}` - Get booking by reference

### Event API (`/api/events`)
- `GET /api/events` - List all events
- `GET /api/events/{id}` - Get event details
- `GET /api/events/upcoming` - Get upcoming events
- `GET /api/events/{id}/seats/available` - Get available seats for an event

### Reservation API (`/api/reservations`)
- `POST /api/reservations` - Create a temporary seat reservation
- `GET /api/reservations/{id}` - Get reservation details
- `DELETE /api/reservations/{id}` - Cancel a reservation

## Architecture Principles

### Domain-Driven Design (DDD)
The project is organized around business domains rather than technical layers, following DDD principles:

**Benefits**:
- **High Cohesion**: Related code is grouped together by domain
- **Low Coupling**: Modules have minimal dependencies on each other
- **Scalability**: Each domain can be extracted into a microservice
- **Clarity**: Business logic is organized by business capability
- **Maintainability**: Changes are isolated to specific domains

**Module Boundaries**:
- **booking**: Owns the booking confirmation workflow
- **event**: Owns event and seat inventory management
- **reservation**: Owns temporary seat reservation logic
- **shared**: Provides cross-cutting infrastructure

### Layered Architecture within Modules
Each module follows a layered architecture:
1. **API Layer** (`api/`) - REST endpoints, request validation
2. **Service Layer** (`service/`) - Business logic, transactions
3. **Domain Layer** (`domain/`) - Entities, business rules
4. **Repository Layer** (`repository/`) - Data access
5. **DTO Layer** (`dto/`) - API contracts
6. **Exception Layer** (`exception/`) - Domain-specific errors

## Design Patterns Used

### 1. Domain-Driven Design (DDD)
- **Bounded Contexts**: Separate modules for booking, event, reservation
- **Ubiquitous Language**: Domain terms in code (Reservation, Booking, Event)
- **Aggregates**: Event + Seats, Booking + BookingSeats
- **Value Objects**: DTOs for immutable data transfer

### 2. Repository Pattern
- Separates data access from business logic
- Panache provides ActiveRecord + Repository patterns
- One repository per aggregate root

### 3. Service Layer Pattern
- Business logic isolated from controllers
- Reusable across different interfaces
- Transaction boundaries defined in services

### 4. DTO Pattern
- API contracts separate from domain models
- Version API independently
- Input validation at API boundary

### 5. Builder Pattern
- Lombok `@Builder` for clean object construction
- Fluent API for complex objects
- Immutable DTOs

### 6. Transaction Script Pattern
- Each service method is a transaction
- Clear transaction boundaries with `@Transactional`
- Automatic rollback on exceptions

### 7. Distributed Lock Pattern
- Redis-based coordination
- Prevents race conditions across multiple instances
- Lua scripts for atomic operations

### 8. Two-Phase Commit (Simplified)
- **Phase 1**: Reservation (temporary seat hold)
- **Phase 2**: Confirmation (payment + booking creation)
- Automatic cleanup on timeout or failure

### 9. Exception Mapper Pattern
- Centralized exception handling
- Consistent error responses across all endpoints
- RFC 7807 Problem Details format

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

### Test Data Management

The project uses two separate data initialization approaches:

#### 1. Unit/Integration Tests (`import.sql`)
- **Location**: `src/test/resources/import.sql`
- **Execution**: Automatic on test run
- **Purpose**: Minimal test data for fast, reliable tests
- **Size**: 3 events, 45 seats
- **Features**:
  - Automatically loaded by Hibernate
  - Fast execution (< 100ms)
  - Recreated for each test run
  - Constants in `TestDataHelper.java`

#### 2. Manual/Demo Testing (`init-sample-data.sh`)
- **Location**: `init-sample-data.sh`
- **Execution**: Manual
- **Purpose**: Rich dataset for manual testing and demos
- **Size**: 3 events, 300 seats
- **Features**:
  - Shell script using direct SQL
  - Checks for existing data
  - More realistic dataset

See [Test Data Guide](TEST_DATA_GUIDE.md) for detailed usage instructions.

### Test Scripts
- `test-booking-flow.sh` - Complete user journey
- `test-race-condition.sh` - Concurrent booking stress test

### Utility Scripts
- `init-sample-data.sh` - Initialize database with sample events and seats
  - Creates 3 sample events (Taylor Swift, Coldplay, Ed Sheeran)
  - Creates 100 seats per event with 3 pricing tiers
  - Checks for existing data to avoid duplication
  - Uses direct SQL for fast initialization

### Manual Testing
1. Start infrastructure: `docker-compose up`
2. Run application: `mvn quarkus:dev`
3. Initialize sample data: `./init-sample-data.sh`
4. Execute test scripts
5. Monitor logs and Redis

### Unit/Integration Testing
1. Test data automatically loaded from `import.sql`
2. Use `TestDataHelper` constants for consistent test data
3. Tests use `create-drop` strategy (clean slate each run)
4. Run tests: `mvn test`

### Load Testing
- Apache Bench (ab)
- JMeter test plans
- Gatling scenarios

## CI/CD Pipeline

### GitHub Actions Workflows

The project uses GitHub Actions for continuous integration and deployment:

#### Develop Branch (JVM Image)
- **Trigger**: Push/PR to `develop`
- **Steps**:
  1. Compile and Test (with PostgreSQL and Redis)
  2. Code Quality Analysis (SonarCloud, SpotBugs, Checkstyle)
  3. Security Scanning (Trivy, OWASP Dependency Check)
  4. Build and Push JVM Docker Image (multi-platform)
- **Image**: `ghcr.io/{owner}/{repo}:develop-latest`
- **Build Time**: ~5-10 minutes

#### Main Branch (Native Image)
- **Trigger**: Push/PR to `main`, Releases
- **Steps**:
  1. Compile and Test (with GraalVM)
  2. Code Quality Analysis (strict quality gates)
  3. Security Scanning (comprehensive scans)
  4. Build and Push Native Docker Image
  5. Performance Testing (on releases)
- **Image**: `ghcr.io/{owner}/{repo}:latest`
- **Build Time**: ~30-45 minutes

#### Pull Request Validation
- **Trigger**: All pull requests
- **Steps**: Quick validation, API compatibility check, code size analysis
- **Purpose**: Fast feedback without full image build

See [GitHub Actions Guide](GITHUB_ACTIONS.md) for detailed documentation.

### Branch Strategy

The repository uses a dual-branch strategy with **main** as the default branch:

```
main (default, production, native builds)
  ↑
  PR from develop
  ↑
develop (JVM builds, integration)
  ↑
  PR from feature branches
  ↑
feature/* (new features)
```

**Branch Purposes**:
- **main** (default): Production-ready code with native GraalVM builds
- **develop**: Integration branch with fast JVM builds for development
- **feature/***: Individual feature development branches

**Workflow**:
1. Create feature branches from `develop`
2. Merge features to `develop` via PR (triggers JVM build)
3. Merge `develop` to `main` via PR for releases (triggers native build)

See [Set Default Branch Guide](SET_DEFAULT_BRANCH.md) for GitHub configuration.

### Image Comparison

| Aspect | JVM (Develop) | Native (Main) |
|--------|---------------|---------------|
| Startup | ~2-3s | ~0.05s |
| Memory | ~200-300 MB | ~50-100 MB |
| Size | ~400-500 MB | ~150-200 MB |
| Build | ~5-10 min | ~30-45 min |
| Platforms | amd64, arm64 | amd64 |

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

### Docker (Local Build)
```bash
# JVM
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t ticket-system .
docker run -p 8080:8080 ticket-system

# Native
mvn package -Dnative -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native-micro -t ticket-system-native .
docker run -p 8080:8080 ticket-system-native
```

### Docker (Pull from Registry)
```bash
# Development JVM image
docker pull ghcr.io/{owner}/{repo}:develop-latest
docker run -d -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/ticketing \
  -e QUARKUS_REDIS_HOSTS=redis://redis:6379 \
  ghcr.io/{owner}/{repo}:develop-latest

# Production native image
docker pull ghcr.io/{owner}/{repo}:latest
docker run -d -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/ticketing \
  -e QUARKUS_REDIS_HOSTS=redis://redis:6379 \
  ghcr.io/{owner}/{repo}:latest
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

**Last Updated**: 2025-11-07
**Version**: 2.0.1
**Maintainer**: Development Team

## Changelog

### Version 2.0.1 (2025-11-07)
- Replaced field injection with constructor injection using Lombok
- Converted `DataInitializationService` to shell script (`init-sample-data.sh`)
- Improved dependency injection patterns across all services
- Added `INIT_DATA_SCRIPT.md` documentation
- Created `import.sql` for automatic test data loading
- Added `TestDataHelper` utility class for consistent test data
- Added `TEST_DATA_GUIDE.md` comprehensive testing documentation
- Separated test data from manual/demo data initialization
- Added GitHub Actions CI/CD workflows:
  - `ci-develop.yml` - JVM image build pipeline for develop branch
  - `ci-main.yml` - Native image build pipeline for main branch
  - `pr-validation.yml` - Quick validation for pull requests
- Added `GITHUB_ACTIONS.md` comprehensive CI/CD documentation
- Configured code quality analysis (SonarCloud, SpotBugs, Checkstyle)
- Integrated security scanning (Trivy, OWASP, Snyk)
- Automated Docker image builds and SBOM generation

### Version 2.0.0 (2025-11-06)
- Refactored to Domain-Driven Design (DDD) architecture
- Organized code into domain modules: booking, event, reservation, shared
- Improved separation of concerns and modularity
- Added centralized exception handling with ExceptionMappers
- Enhanced documentation with DDD principles and architecture details
- Added integration tests
- Improved test coverage and documentation
