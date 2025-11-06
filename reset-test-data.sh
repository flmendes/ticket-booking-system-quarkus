#!/bin/bash

# Script to reset test data between Gatling test runs
# This cleans up test bookings and resets seats to AVAILABLE status
# Uses the PostgreSQL container from docker-compose.yml

set -e

# PostgreSQL container name from docker-compose.yml
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-postgres-ticket}"

# Database parameters (matching docker-compose.yml)
DB_NAME="${DB_NAME:-ticketdb}"
DB_USER="${DB_USER:-ticketuser}"

echo "======================================"
echo "Resetting Test Data"
echo "======================================"
echo "Container: $POSTGRES_CONTAINER"
echo "Database: $DB_NAME"
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

# Execute reset commands using docker exec
docker exec -i "$POSTGRES_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << SQL
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
    'Seats reset successfully!' as message,
    COUNT(CASE WHEN status = 'AVAILABLE' THEN 1 END) as available_seats,
    COUNT(CASE WHEN status = 'RESERVED' THEN 1 END) as reserved_seats,
    COUNT(CASE WHEN status = 'BOOKED' THEN 1 END) as booked_seats
FROM seats
WHERE event_id IN (SELECT event_id FROM events WHERE event_name LIKE 'Load Test Event%');
SQL

echo ""
echo "======================================"
echo "Test data reset complete!"
echo "All test seats are now AVAILABLE"
echo "Test bookings and reservations removed"
echo "======================================"
