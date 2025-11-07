# Sample Data Initialization Script

## Overview

The `init-sample-data.sh` script initializes the database with sample events and seats for testing purposes. This script replaces the former `DataInitializationService` Java class.

## Features

- Creates 3 sample events:
  - **Taylor Swift - Eras Tour** (30 days from now) at MetLife Stadium
  - **Coldplay - Music of the Spheres** (45 days from now) at Madison Square Garden
  - **Ed Sheeran - Mathematics Tour** (60 days from now) at Barclays Center

- Creates 100 sample seats for each event with 3 different categories:
  - **VIP**: $299.99
  - **Premium**: $149.99
  - **Regular**: $79.99

- Checks if database already has data to avoid duplication
- Provides colored console output for better readability
- Validates database connection before initialization

## Prerequisites

- PostgreSQL client (`psql`) must be installed
- Database must be running and accessible
- Valid database credentials

## Usage

### Basic Usage

```bash
./init-sample-data.sh
```

### With Custom Configuration

You can override default settings using environment variables:

```bash
# Custom database connection
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=ticketing
export POSTGRES_USER=ticketing_user
export POSTGRES_PASSWORD=ticketing_pass

./init-sample-data.sh
```

### Using Docker Compose

If you're using the provided `docker-compose.yml`:

```bash
# Start services
docker-compose up -d

# Wait for database to be ready
sleep 5

# Run initialization script
./init-sample-data.sh
```

## Configuration

The script accepts the following environment variables:

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `POSTGRES_HOST` | `localhost` | PostgreSQL host |
| `POSTGRES_PORT` | `5432` | PostgreSQL port |
| `POSTGRES_DB` | `ticketing` | Database name |
| `POSTGRES_USER` | `ticketing_user` | Database user |
| `POSTGRES_PASSWORD` | `ticketing_pass` | Database password |
| `API_BASE_URL` | `http://localhost:8080` | Application API base URL (reserved for future use) |

## Output

The script provides informative console output:

```bash
====================================
Sample Data Initialization Script
====================================

[INFO] Testing database connection...
[INFO] Database connection successful!

[INFO] Checking if database already has data...
[INFO] Database is empty. Proceeding with initialization...

[INFO] Starting sample data initialization...
[INFO] Creating event: Taylor Swift - Eras Tour
[INFO] Created event: Taylor Swift - Eras Tour (ID: 1)
[INFO] Creating 100 seats for event: Taylor Swift - Eras Tour
[INFO] Created 100 seats for event: Taylor Swift - Eras Tour
[INFO] Creating event: Coldplay - Music of the Spheres
[INFO] Created event: Coldplay - Music of the Spheres (ID: 2)
[INFO] Creating 100 seats for event: Coldplay - Music of the Spheres
[INFO] Created 100 seats for event: Coldplay - Music of the Spheres
[INFO] Creating event: Ed Sheeran - Mathematics Tour
[INFO] Created event: Ed Sheeran - Mathematics Tour (ID: 3)
[INFO] Creating 100 seats for event: Ed Sheeran - Mathematics Tour
[INFO] Created 100 seats for event: Ed Sheeran - Mathematics Tour
[INFO] Sample data initialized successfully!

====================================
Initialization complete!
====================================
```

## Error Handling

The script includes comprehensive error handling:

- **Missing psql**: Exits with error if PostgreSQL client is not installed
- **Connection failure**: Validates database connection before proceeding
- **Existing data**: Skips initialization if events already exist
- **SQL errors**: Logs detailed error messages for debugging

## Integration with Application Startup

### Manual Initialization (Recommended)

Run the script manually after starting the application:

```bash
# Start application
./mvnw quarkus:dev

# In another terminal, initialize data
./init-sample-data.sh
```

### Automated Initialization

Add to your `docker-compose.yml` or startup script:

```yaml
services:
  postgres:
    # ... postgres configuration ...

  app:
    # ... app configuration ...
    depends_on:
      - postgres
    command: >
      sh -c "
        sleep 5 &&
        ./init-sample-data.sh &&
        java -jar app.jar
      "
```

## Troubleshooting

### psql command not found

**Problem**: The script requires PostgreSQL client tools.

**Solution**: Install PostgreSQL client:

```bash
# macOS
brew install postgresql

# Ubuntu/Debian
sudo apt-get install postgresql-client

# CentOS/RHEL
sudo yum install postgresql
```

### Cannot connect to database

**Problem**: Database connection fails.

**Solution**: Verify:
1. Database is running: `docker ps` or `systemctl status postgresql`
2. Connection parameters are correct
3. Database exists: `psql -h localhost -U ticketing_user -l`

### Database already has data

**Problem**: Script exits saying data already exists.

**Solution**: This is expected behavior. To reinitialize:

```bash
# Drop and recreate database
docker-compose down -v
docker-compose up -d
sleep 5
./init-sample-data.sh
```

## Data Structure

### Events Table

Each event includes:
- Event name
- Event date (future date)
- Venue name
- Total seats capacity
- Available seats count
- Status: ON_SALE
- Sale start time (yesterday)

### Seats Table

Each seat includes:
- Event ID (foreign key)
- Seat number (e.g., A1, B5)
- Section (VIP, Premium, Regular)
- Row number (A, B, C, ...)
- Seat type (VIP, PREMIUM, REGULAR)
- Price (based on seat type)
- Status: AVAILABLE
- Version (for optimistic locking)

## Comparison with Previous Java Service

| Aspect | Java Service | Shell Script |
|--------|-------------|--------------|
| **Execution** | Automatic on startup | Manual or scripted |
| **Control** | Limited (runs on every startup) | Full control over when to run |
| **Dependencies** | Requires Java application | Independent (psql only) |
| **Performance** | Slower (JVM overhead) | Faster (direct SQL) |
| **Flexibility** | Hardcoded in Java | Easy to modify |
| **Testing** | Requires application restart | Can run independently |

## Advantages

1. **Independence**: Runs without starting the application
2. **Flexibility**: Easy to customize or extend
3. **Speed**: Direct SQL is faster than JPA
4. **Control**: Explicit execution, no surprises on startup
5. **Debugging**: Easier to troubleshoot with verbose output
6. **Portability**: Works with any PostgreSQL database

## See Also

- [Project Structure](PROJECT_STRUCTURE.md)
- [Quick Start Guide](QUICKSTART.md)
- [Test Scripts](README_SCRIPTS.md)
- [Docker Setup](DOCKER_SCRIPTS_USAGE.md)

---

**Last Updated**: 2025-11-07
**Author**: Development Team
