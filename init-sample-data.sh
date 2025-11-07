#!/bin/bash

# init-sample-data.sh
# Script to initialize sample data for the ticket booking system
# This replaces the DataInitializationService Java class

set -e  # Exit on error

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_DB="${POSTGRES_DB:-ticketing}"
POSTGRES_USER="${POSTGRES_USER:-ticketing_user}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-ticketing_pass}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if database is empty
check_database() {
    log_info "Checking if database already has data..."

    local event_count=$(PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -t -c "SELECT COUNT(*) FROM events;" 2>/dev/null || echo "0")
    event_count=$(echo $event_count | tr -d ' ')

    if [ "$event_count" -gt 0 ]; then
        log_warn "Database already has $event_count events. Skipping initialization."
        return 1
    fi

    log_info "Database is empty. Proceeding with initialization..."
    return 0
}

# Calculate future date in ISO format
get_future_date() {
    local days=$1
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        date -v+${days}d -u +"%Y-%m-%dT%H:%M:%S"
    else
        # Linux
        date -d "+${days} days" -u +"%Y-%m-%dT%H:%M:%S"
    fi
}

# Get yesterday's date in ISO format
get_yesterday() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        date -v-1d -u +"%Y-%m-%dT%H:%M:%S"
    else
        # Linux
        date -d "yesterday" -u +"%Y-%m-%dT%H:%M:%S"
    fi
}

# Create an event using direct SQL
create_event_sql() {
    local name="$1"
    local event_date="$2"
    local venue="$3"
    local total_seats=$4
    local sale_start=$(get_yesterday)

    log_info "Creating event: $name"

    local sql="INSERT INTO events (event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, version, created_at)
               VALUES ('$name', '$event_date', '$venue', $total_seats, $total_seats, 'ON_SALE', '$sale_start', 0, CURRENT_TIMESTAMP)
               RETURNING event_id;"

    local event_id=$(PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -t -c "$sql" 2>&1)

    if [ $? -ne 0 ]; then
        log_error "Failed to create event: $name"
        log_error "$event_id"
        return 1
    fi

    event_id=$(echo $event_id | tr -d ' ')
    log_info "Created event: $name (ID: $event_id)"
    echo $event_id
}

# Create seats for an event using direct SQL
create_seats_for_event() {
    local event_id=$1
    local event_name="$2"
    local num_seats=$3

    log_info "Creating $num_seats seats for event: $event_name"

    local sections=("VIP" "Premium" "Regular")
    local prices=("299.99" "149.99" "79.99")
    local seat_types=("VIP" "PREMIUM" "REGULAR")

    local values=""

    for ((i=1; i<=num_seats; i++)); do
        local section_index=$((i % 3))
        local section="${sections[$section_index]}"
        local price="${prices[$section_index]}"
        local seat_type="${seat_types[$section_index]}"

        # Calculate row (A, B, C, etc.)
        local row_char=$(printf \\$(printf '%03o' $((65 + i / 10))))
        local seat_number="${row_char}$((i % 10))"

        if [ -n "$values" ]; then
            values+=","
        fi

        values+="($event_id, '$seat_number', '$section', '$row_char', '$seat_type', $price, 'AVAILABLE', 0, CURRENT_TIMESTAMP)"
    done

    local sql="INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
               VALUES $values;"

    PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "$sql" > /dev/null 2>&1

    if [ $? -ne 0 ]; then
        log_error "Failed to create seats for event: $event_name"
        return 1
    fi

    log_info "Created $num_seats seats for event: $event_name"
}

# Main initialization function
initialize_data() {
    log_info "Starting sample data initialization..."

    # Create events
    local taylor_date=$(get_future_date 30)
    local coldplay_date=$(get_future_date 45)
    local ed_date=$(get_future_date 60)

    # Create Taylor Swift event
    local taylor_id=$(create_event_sql "Taylor Swift - Eras Tour" "$taylor_date" "MetLife Stadium" 10000)
    if [ $? -eq 0 ] && [ -n "$taylor_id" ]; then
        create_seats_for_event "$taylor_id" "Taylor Swift - Eras Tour" 100
    fi

    # Create Coldplay event
    local coldplay_id=$(create_event_sql "Coldplay - Music of the Spheres" "$coldplay_date" "Madison Square Garden" 5000)
    if [ $? -eq 0 ] && [ -n "$coldplay_id" ]; then
        create_seats_for_event "$coldplay_id" "Coldplay - Music of the Spheres" 100
    fi

    # Create Ed Sheeran event
    local ed_id=$(create_event_sql "Ed Sheeran - Mathematics Tour" "$ed_date" "Barclays Center" 8000)
    if [ $? -eq 0 ] && [ -n "$ed_id" ]; then
        create_seats_for_event "$ed_id" "Ed Sheeran - Mathematics Tour" 100
    fi

    log_info "Sample data initialized successfully!"
}

# Main execution
main() {
    log_info "===================================="
    log_info "Sample Data Initialization Script"
    log_info "===================================="
    echo ""

    # Check if psql is available
    if ! command -v psql &> /dev/null; then
        log_error "psql command not found. Please install PostgreSQL client."
        exit 1
    fi

    # Check database connection
    log_info "Testing database connection..."
    if ! PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB -c "SELECT 1;" > /dev/null 2>&1; then
        log_error "Cannot connect to database. Please check your configuration."
        log_error "Host: $POSTGRES_HOST:$POSTGRES_PORT"
        log_error "Database: $POSTGRES_DB"
        log_error "User: $POSTGRES_USER"
        exit 1
    fi
    log_info "Database connection successful!"
    echo ""

    # Check if database already has data
    if ! check_database; then
        exit 0
    fi

    echo ""

    # Initialize data
    initialize_data

    echo ""
    log_info "===================================="
    log_info "Initialization complete!"
    log_info "===================================="
}

# Run main function
main "$@"
