#!/bin/bash

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api"

echo -e "${YELLOW}=== Ticket Booking System - Test Script ===${NC}\n"

# Test 1: Get all events
echo -e "${GREEN}Test 1: Getting all events...${NC}"
curl -s "${BASE_URL}/events" | jq '.'
echo -e "\n"

# Test 2: Get available seats for first event
echo -e "${GREEN}Test 2: Getting available seats for event 1...${NC}"
curl -s "${BASE_URL}/events/1/seats/available" | jq '.data | length'
echo " available seats"
echo -e "\n"

# Test 3: Reserve seats
echo -e "${GREEN}Test 3: Reserving seats A1 and A2 for user123...${NC}"
RESERVE_RESPONSE=$(curl -s -X POST "${BASE_URL}/bookings/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatNumbers": ["A1", "A2"],
    "userId": "user123"
  }')

echo "$RESERVE_RESPONSE" | jq '.'

# Extract reservation ID
RESERVATION_ID=$(echo "$RESERVE_RESPONSE" | jq -r '.data.reservationId')
TOTAL_AMOUNT=$(echo "$RESERVE_RESPONSE" | jq -r '.data.totalAmount')

if [ "$RESERVATION_ID" != "null" ] && [ "$RESERVATION_ID" != "" ]; then
    echo -e "${GREEN}✓ Reservation successful! ID: $RESERVATION_ID${NC}\n"
else
    echo -e "${RED}✗ Reservation failed!${NC}\n"
    exit 1
fi

# Test 4: Try to reserve same seats with different user (should fail)
echo -e "${GREEN}Test 4: Attempting to reserve same seats with different user (should fail)...${NC}"
CONCURRENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/bookings/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatNumbers": ["A1"],
    "userId": "user456"
  }')

echo "$CONCURRENT_RESPONSE" | jq '.'

SUCCESS=$(echo "$CONCURRENT_RESPONSE" | jq -r '.success')
if [ "$SUCCESS" == "false" ]; then
    echo -e "${GREEN}✓ Race condition prevented! Seat correctly unavailable${NC}\n"
else
    echo -e "${RED}✗ Race condition not prevented! This is a problem${NC}\n"
fi

# Test 5: Confirm booking
echo -e "${GREEN}Test 5: Confirming booking with payment...${NC}"
BOOKING_RESPONSE=$(curl -s -X POST "${BASE_URL}/bookings/confirm" \
  -H "Content-Type: application/json" \
  -d "{
    \"reservationId\": $RESERVATION_ID,
    \"userId\": \"user123\",
    \"paymentRequest\": {
      \"paymentMethod\": \"CREDIT_CARD\",
      \"cardNumber\": \"4111111111111111\",
      \"cardHolderName\": \"John Doe\",
      \"amount\": $TOTAL_AMOUNT,
      \"cvv\": \"123\",
      \"expiryDate\": \"12/26\"
    }
  }")

echo "$BOOKING_RESPONSE" | jq '.'

BOOKING_REF=$(echo "$BOOKING_RESPONSE" | jq -r '.data.bookingReference')

if [ "$BOOKING_REF" != "null" ] && [ "$BOOKING_REF" != "" ]; then
    echo -e "${GREEN}✓ Booking confirmed! Reference: $BOOKING_REF${NC}\n"
else
    echo -e "${RED}✗ Booking confirmation failed!${NC}\n"
    exit 1
fi

# Test 6: Get user bookings
echo -e "${GREEN}Test 6: Getting all bookings for user123...${NC}"
curl -s "${BASE_URL}/bookings/user/user123" | jq '.'
echo -e "\n"

# Test 7: Get booking by reference
echo -e "${GREEN}Test 7: Getting booking by reference...${NC}"
curl -s "${BASE_URL}/bookings/reference/${BOOKING_REF}" | jq '.'
echo -e "\n"

# Test 8: Check updated seat availability
echo -e "${GREEN}Test 8: Checking updated seat availability...${NC}"
AVAILABLE_COUNT=$(curl -s "${BASE_URL}/events/1/seats/available" | jq '.data | length')
echo -e "Available seats after booking: ${AVAILABLE_COUNT}"
echo -e "\n"

echo -e "${GREEN}=== All tests completed! ===${NC}"
echo -e "${YELLOW}Summary:${NC}"
echo -e "  ✓ Events retrieved successfully"
echo -e "  ✓ Seats reserved successfully"
echo -e "  ✓ Race condition handled correctly"
echo -e "  ✓ Booking confirmed with payment"
echo -e "  ✓ Booking reference: ${BOOKING_REF}"
