#!/bin/bash

# Script to run Gatling Booking Simulation
# Usage: ./run-gatling-booking.sh [options]
#
# Options:
#   -u, --users <number>        Number of concurrent users (default: 50)
#   -r, --ramp <seconds>        Ramp-up duration in seconds (default: 10)
#   -d, --duration <seconds>    Test duration in seconds (default: 60)
#   -b, --base-url <url>        Base URL of the application (default: http://localhost:8080)

set -e

# Default values
USERS=50
RAMP_DURATION=10
DURATION=60
BASE_URL="http://localhost:8080"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--users)
            USERS="$2"
            shift 2
            ;;
        -r|--ramp)
            RAMP_DURATION="$2"
            shift 2
            ;;
        -d|--duration)
            DURATION="$2"
            shift 2
            ;;
        -b|--base-url)
            BASE_URL="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [-u users] [-r ramp] [-d duration] [-b base-url]"
            exit 1
            ;;
    esac
done

echo "======================================"
echo "Gatling Booking Simulation"
echo "======================================"
echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Concurrent Users: $USERS"
echo "  Ramp Duration: ${RAMP_DURATION}s"
echo "  Test Duration: ${DURATION}s"
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
echo "Starting Gatling test..."
echo ""

# Run Gatling simulation
./mvnw gatling:test \
    -Dgatling.simulationClass=com.ticketing.gatling.BookingSimulation \
    -DbaseUrl="$BASE_URL" \
    -Dusers="$USERS" \
    -DrampDuration="$RAMP_DURATION" \
    -Dduration="$DURATION"

echo ""
echo "======================================"
echo "Test completed!"
echo "Check the Gatling report in target/gatling/"
echo "======================================"
