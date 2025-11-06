#!/bin/bash

# Script to seed test data for Gatling load tests
# This creates events and seats in the database
# Uses the PostgreSQL container from docker-compose.yml

set -e

# PostgreSQL container name from docker-compose.yml
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-postgres-ticket}"

# Database parameters (matching docker-compose.yml)
DB_NAME="${DB_NAME:-ticketdb}"
DB_USER="${DB_USER:-ticketuser}"

echo "======================================"
echo "Seeding Test Data for Load Tests"
echo "======================================"
echo "Container: $POSTGRES_CONTAINER"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "======================================"
echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker command not found."
    echo "Please install Docker to run this script."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker daemon is not running."
    echo "Please start Docker and try again."
    exit 1
fi

# Check if PostgreSQL container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    echo "ERROR: PostgreSQL container '$POSTGRES_CONTAINER' is not running."
    echo ""
    echo "Please start the containers first:"
    echo "  docker-compose up -d"
    echo ""
    echo "Or check the container name with:"
    echo "  docker ps"
    exit 1
fi

# Test connection
echo "Testing database connection..."
if docker exec "$POSTGRES_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    echo "Connection successful!"
else
    echo "ERROR: Cannot connect to database in container '$POSTGRES_CONTAINER'."
    echo "Please check if the container is healthy:"
    echo "  docker ps"
    echo "  docker logs $POSTGRES_CONTAINER"
    exit 1
fi

echo ""
echo "Running seed script..."
echo ""

# Get the absolute path of the SQL file
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="$SCRIPT_DIR/src/test/resources/test-data-seed.sql"

if [ ! -f "$SQL_FILE" ]; then
    echo "ERROR: SQL file not found at $SQL_FILE"
    exit 1
fi

# Execute the seed script by piping it into the container
docker exec -i "$POSTGRES_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "$SQL_FILE"

echo ""
echo "======================================"
echo "Test data seeded successfully!"
echo ""
echo "Created:"
echo "  - 5 test events"
echo "  - 2000 total seats across all events"
echo ""
echo "You can now run Gatling load tests:"
echo "  ./run-gatling-booking.sh"
echo "  ./run-gatling-race.sh"
echo "  ./run-gatling-stress.sh"
echo "======================================"
