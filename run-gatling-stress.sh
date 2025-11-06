#!/bin/bash

# Script to run Gatling Stress Test Simulation
# Usage: ./run-gatling-stress.sh [options]
#
# Options:
#   -n, --normal <number>       Normal load (users/sec) (default: 10)
#   -s, --stress <number>       Stress load (users/sec) (default: 100)
#   -p, --spike <number>        Spike load (users/sec) (default: 500)
#   -b, --base-url <url>        Base URL of the application (default: http://localhost:8080)

set -e

# Default values
NORMAL_LOAD=10
STRESS_LOAD=100
SPIKE_LOAD=500
BASE_URL="http://localhost:8080"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--normal)
            NORMAL_LOAD="$2"
            shift 2
            ;;
        -s|--stress)
            STRESS_LOAD="$2"
            shift 2
            ;;
        -p|--spike)
            SPIKE_LOAD="$2"
            shift 2
            ;;
        -b|--base-url)
            BASE_URL="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [-n normal] [-s stress] [-p spike] [-b base-url]"
            exit 1
            ;;
    esac
done

echo "======================================"
echo "Gatling Stress Test Simulation"
echo "======================================"
echo "Configuration:"
echo "  Base URL: $BASE_URL"
echo "  Normal Load: ${NORMAL_LOAD} users/sec"
echo "  Stress Load: ${STRESS_LOAD} users/sec"
echo "  Spike Load: ${SPIKE_LOAD} users/sec"
echo "======================================"
echo ""
echo "Test Phases:"
echo "  1. Baseline: ${NORMAL_LOAD} users/sec for 30s"
echo "  2. Ramp up: ${NORMAL_LOAD} -> ${STRESS_LOAD} users/sec over 60s"
echo "  3. Sustained stress: ${STRESS_LOAD} users/sec for 120s"
echo "  4. Spike: ${STRESS_LOAD} -> ${SPIKE_LOAD} users/sec for 30s"
echo "  5. Recovery: ${SPIKE_LOAD} -> ${NORMAL_LOAD} users/sec over 60s"
echo "  Total duration: ~6 minutes"
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
echo "Starting Stress Test..."
echo "WARNING: This test will put significant load on your system!"
echo ""

# Run Gatling simulation
./mvnw gatling:test \
    -Dgatling.simulationClass=com.ticketing.gatling.StressTestSimulation \
    -DbaseUrl="$BASE_URL" \
    -DnormalLoad="$NORMAL_LOAD" \
    -DstressLoad="$STRESS_LOAD" \
    -DspikeLoad="$SPIKE_LOAD"

echo ""
echo "======================================"
echo "Test completed!"
echo "Check the Gatling report in target/gatling/"
echo "======================================"
