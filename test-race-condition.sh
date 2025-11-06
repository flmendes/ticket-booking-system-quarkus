#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080/api"
SEAT_TO_TEST="B5"
EVENT_ID=1
NUM_CONCURRENT=20

echo -e "${YELLOW}=== Concurrent Booking Test - Race Condition Demo ===${NC}\n"
echo -e "${BLUE}Testing: $NUM_CONCURRENT users trying to book seat $SEAT_TO_TEST simultaneously${NC}\n"

# Create a temporary file to store results
RESULTS_FILE=$(mktemp)

# Function to attempt booking
attempt_booking() {
    local user_id=$1
    local result=$(curl -s -X POST "${BASE_URL}/bookings/reserve" \
      -H "Content-Type: application/json" \
      -d "{
        \"eventId\": ${EVENT_ID},
        \"seatNumbers\": [\"${SEAT_TO_TEST}\"],
        \"userId\": \"concurrent_user_${user_id}\"
      }")

    local success=$(echo "$result" | jq -r '.success')
    echo "${user_id}:${success}" >> "$RESULTS_FILE"

    if [ "$success" == "true" ]; then
        echo -e "${GREEN}User $user_id: SUCCESS ✓${NC}"
    else
        echo -e "${RED}User $user_id: FAILED (seat not available)${NC}"
    fi
}

echo -e "${YELLOW}Starting concurrent booking attempts...${NC}\n"

# Launch concurrent requests
for i in $(seq 1 $NUM_CONCURRENT); do
    attempt_booking $i &
done

# Wait for all background jobs to complete
wait

echo -e "\n${YELLOW}Analyzing results...${NC}\n"

# Count successes and failures
SUCCESS_COUNT=$(grep -c ":true" "$RESULTS_FILE")
FAILURE_COUNT=$(grep -c ":false" "$RESULTS_FILE")

echo -e "${BLUE}Results Summary:${NC}"
echo -e "  Total attempts: $NUM_CONCURRENT"
echo -e "  ${GREEN}Successful bookings: $SUCCESS_COUNT${NC}"
echo -e "  ${RED}Failed bookings: $FAILURE_COUNT${NC}"
echo -e ""

if [ $SUCCESS_COUNT -eq 1 ]; then
    echo -e "${GREEN}✓ PASS: Exactly 1 booking succeeded${NC}"
    echo -e "${GREEN}✓ Race condition handled correctly!${NC}"
    echo -e "${GREEN}✓ Distributed locking working as expected${NC}"

    # Show which user succeeded
    WINNER=$(grep ":true" "$RESULTS_FILE" | cut -d':' -f1)
    echo -e "\n${BLUE}Winner: User $WINNER${NC}"
elif [ $SUCCESS_COUNT -eq 0 ]; then
    echo -e "${YELLOW}⚠ All bookings failed (seat might already be booked)${NC}"
elif [ $SUCCESS_COUNT -gt 1 ]; then
    echo -e "${RED}✗ FAIL: Multiple bookings succeeded ($SUCCESS_COUNT)${NC}"
    echo -e "${RED}✗ Race condition NOT handled correctly!${NC}"
    echo -e "${RED}✗ Double-booking detected - this is a critical issue!${NC}"
fi

# Cleanup
rm -f "$RESULTS_FILE"

echo -e "\n${YELLOW}=== Test Complete ===${NC}"
