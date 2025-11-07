package com.ticketing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test Data Helper - Constants and utilities for test data
 *
 * This class provides constants that match the data loaded by import.sql
 * for use in unit and integration tests.
 */
public class TestDataHelper {

    // Event IDs
    public static final Long EVENT_ROCK_CONCERT_ID = 1L;
    public static final Long EVENT_THEATER_SHOW_ID = 2L;
    public static final Long EVENT_SPORTS_GAME_ID = 3L;

    // Event Names
    public static final String EVENT_ROCK_CONCERT_NAME = "Rock Concert 2025";
    public static final String EVENT_THEATER_SHOW_NAME = "Broadway Show";
    public static final String EVENT_SPORTS_GAME_NAME = "Championship Game";

    // Venue Names
    public static final String VENUE_STADIUM_ARENA = "Stadium Arena";
    public static final String VENUE_CITY_THEATER = "City Theater";
    public static final String VENUE_SPORTS_COMPLEX = "Sports Complex";

    // Seat Counts per Event
    public static final int EVENT_1_TOTAL_SEATS = 20;
    public static final int EVENT_2_TOTAL_SEATS = 15;
    public static final int EVENT_3_TOTAL_SEATS = 10;

    // Seat IDs for Event 1 (Rock Concert)
    public static final class Event1Seats {
        // VIP Seats (A1-A5)
        public static final Long VIP_A1 = 1L;
        public static final Long VIP_A2 = 2L;
        public static final Long VIP_A3 = 3L;
        public static final Long VIP_A4 = 4L;
        public static final Long VIP_A5 = 5L;

        // Premium Seats (B1-B5, C1-C5)
        public static final Long PREMIUM_B1 = 6L;
        public static final Long PREMIUM_B2 = 7L;
        public static final Long PREMIUM_B3 = 8L;
        public static final Long PREMIUM_B4 = 9L;
        public static final Long PREMIUM_B5 = 10L;
        public static final Long PREMIUM_C1 = 11L;
        public static final Long PREMIUM_C2 = 12L;
        public static final Long PREMIUM_C3 = 13L;
        public static final Long PREMIUM_C4 = 14L;
        public static final Long PREMIUM_C5 = 15L;

        // Regular Seats (D1-D5)
        public static final Long REGULAR_D1 = 16L;
        public static final Long REGULAR_D2 = 17L;
        public static final Long REGULAR_D3 = 18L;
        public static final Long REGULAR_D4 = 19L;
        public static final Long REGULAR_D5 = 20L;
    }

    // Seat Numbers
    public static final class SeatNumbers {
        // Event 1 VIP
        public static final String A1 = "A1";
        public static final String A2 = "A2";
        public static final String A3 = "A3";
        public static final String A4 = "A4";
        public static final String A5 = "A5";

        // Event 1 Premium
        public static final String B1 = "B1";
        public static final String B2 = "B2";
        public static final String B3 = "B3";
        public static final String B4 = "B4";
        public static final String B5 = "B5";
        public static final String C1 = "C1";
        public static final String C2 = "C2";
        public static final String C3 = "C3";
        public static final String C4 = "C4";
        public static final String C5 = "C5";

        // Event 1 Regular
        public static final String D1 = "D1";
        public static final String D2 = "D2";
        public static final String D3 = "D3";
        public static final String D4 = "D4";
        public static final String D5 = "D5";
    }

    // Prices
    public static final class Prices {
        // Event 1 (Rock Concert)
        public static final BigDecimal EVENT_1_VIP = new BigDecimal("299.99");
        public static final BigDecimal EVENT_1_PREMIUM = new BigDecimal("149.99");
        public static final BigDecimal EVENT_1_REGULAR = new BigDecimal("79.99");

        // Event 2 (Theater Show)
        public static final BigDecimal EVENT_2_VIP = new BigDecimal("199.99");
        public static final BigDecimal EVENT_2_PREMIUM = new BigDecimal("99.99");
        public static final BigDecimal EVENT_2_REGULAR = new BigDecimal("49.99");

        // Event 3 (Sports Game)
        public static final BigDecimal EVENT_3_VIP = new BigDecimal("399.99");
        public static final BigDecimal EVENT_3_PREMIUM = new BigDecimal("199.99");
        public static final BigDecimal EVENT_3_REGULAR = new BigDecimal("99.99");
    }

    // Test User IDs
    public static final class TestUsers {
        public static final String USER_1 = "user-test-001";
        public static final String USER_2 = "user-test-002";
        public static final String USER_3 = "user-test-003";
        public static final String USER_ADMIN = "admin-test-001";
    }

    // Test Payment Data
    public static final class TestPayments {
        public static final String CARD_NUMBER_VALID = "4111111111111111";
        public static final String CARD_NUMBER_INVALID = "0000000000000000";
        public static final String CVV_VALID = "123";
        public static final String CARDHOLDER_NAME = "Test User";

        public static String getValidExpiryDate() {
            // Returns a date 2 years in the future in MM/YY format
            LocalDateTime future = LocalDateTime.now().plusYears(2);
            int month = future.getMonthValue();
            int year = future.getYear() % 100;
            return String.format("%02d/%02d", month, year);
        }
    }

    // Utility methods

    /**
     * Get future event date (30 days from now)
     */
    public static LocalDateTime getFutureEventDate() {
        return LocalDateTime.now().plusDays(30);
    }

    /**
     * Get past event date (30 days ago)
     */
    public static LocalDateTime getPastEventDate() {
        return LocalDateTime.now().minusDays(30);
    }

    /**
     * Get reservation expiry time (10 minutes from now)
     */
    public static LocalDateTime getReservationExpiryTime() {
        return LocalDateTime.now().plusMinutes(10);
    }

    /**
     * Get expired reservation time (10 minutes ago)
     */
    public static LocalDateTime getExpiredReservationTime() {
        return LocalDateTime.now().minusMinutes(10);
    }

    // Private constructor to prevent instantiation
    private TestDataHelper() {
        throw new AssertionError("TestDataHelper is a utility class and should not be instantiated");
    }
}
