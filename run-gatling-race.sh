#!/bin/bash

# Script to run Gatling Race Condition Simulation
# Usage: ./run-gatling-race.sh [options]
#
# Options:
#   -u, --users <number>        Number of concurrent users (default: 100)
#   -s, --seat <seat>           Target seat number (default: A1)
#   -e, --event-id <id>         Event ID to test (default: 1)
#   -b, --base-url <url>        Base URL of the application (default: http://localhost:8080)

set -e

# Default values
USERS=100
TARGET_SEAT="A1"
EVENT_ID=1
BASE_URL="http://localhost:8080"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--users)
            USERS="$2"
            shift 2
            ;;
        -s|--seat)
            TARGET_SEAT="$2"
            shift 2
            ;;
        -e|--event-id)
            EVENT_ID="$2"
            shift 2
            ;;
        -b|--base-url)
            BASE_URL="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [-u users] [-s seat] [-e event-id] [-b base-url]"
            exit 1
            ;;
    esac
done

echo "======================================"
echo "Gatling Race Condition Simulation"
echo "======================================"
echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Concurrent Users: $USERS"
echo "  Target Seat: $TARGET_SEAT"
echo "  Event ID: $EVENT_ID"
echo "======================================"
echo ""

# Check if application is running
echo "Checking if application is running at $BASE_URL..."
if curl -s -f "$BASE_URL/api/bookings/health" > /dev/null 2>&1; then
    echo "Application is running!"
else
    echo "WARNING: Application may not be running at $BASE_URL"
    echo "Make sure to start the application before running the test"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "Starting Race Condition test..."
echo "This test will have multiple users compete for the same seats"
echo ""

# Run Gatling simulation
./mvnw gatling:test \
    -Dgatling.simulationClass=com.ticketing.gatling.RaceConditionSimulation \
    -DbaseUrl="$BASE_URL" \
    -Dusers="$USERS" \
    -DtargetSeat="$TARGET_SEAT" \
    -DeventId="$EVENT_ID"

echo ""
echo "======================================"
echo "Test completed!"
echo "Check the Gatling report in target/gatling/"
echo "======================================"
