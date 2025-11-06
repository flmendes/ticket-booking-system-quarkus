#!/bin/bash

# Script to fix NULL version fields in the database
# This must be run BEFORE starting the application with the fixed code

set -e

# PostgreSQL container name from docker-compose.yml
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-postgres-ticket}"

# Database parameters
DB_NAME="${DB_NAME:-ticketdb}"
DB_USER="${DB_USER:-ticketuser}"

echo "=========================================="
echo "Fixing NULL version fields in database"
echo "=========================================="
echo "Container: $POSTGRES_CONTAINER"
echo "Database: $DB_NAME"
echo "=========================================="
echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker command not found."
    exit 1
fi

# Check if PostgreSQL container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    echo "ERROR: PostgreSQL container '$POSTGRES_CONTAINER' is not running."
    echo "Please start it with: docker-compose up -d"
    exit 1
fi

echo "Executing fix script..."
echo ""

# Get the absolute path of the SQL file
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="$SCRIPT_DIR/fix-null-versions.sql"

if [ ! -f "$SQL_FILE" ]; then
    echo "ERROR: SQL file not found at $SQL_FILE"
    exit 1
fi

# Execute the fix script
docker exec -i "$POSTGRES_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < "$SQL_FILE"

echo ""
echo "=========================================="
echo "Database fixed successfully!"
echo ""
echo "Next steps:"
echo "1. Restart the application: ./mvnw quarkus:dev"
echo "2. Run load tests: ./run-gatling-booking.sh"
echo "=========================================="
