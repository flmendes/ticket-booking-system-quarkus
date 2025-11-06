-- Test Data Seed Script for Gatling Load Tests
-- This script creates events and seats for load testing

-- Clean up existing test data (optional - comment out if you want to preserve data)
-- DELETE FROM booking_seats WHERE booking_id IN (SELECT booking_id FROM bookings WHERE user_id LIKE 'user-%' OR user_id LIKE 'race-user-%' OR user_id LIKE 'overlap-user-%' OR user_id LIKE 'stress-user-%');
-- DELETE FROM bookings WHERE user_id LIKE 'user-%' OR user_id LIKE 'race-user-%' OR user_id LIKE 'overlap-user-%' OR user_id LIKE 'stress-user-%';
-- DELETE FROM reservations WHERE user_id LIKE 'user-%' OR user_id LIKE 'race-user-%' OR user_id LIKE 'overlap-user-%' OR user_id LIKE 'stress-user-%';
-- DELETE FROM seats WHERE event_id IN (SELECT event_id FROM events WHERE event_name LIKE 'Load Test Event%');
-- DELETE FROM events WHERE event_name LIKE 'Load Test Event%';

-- Create test events
INSERT INTO events (event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, created_at)
VALUES
    ('Load Test Event 1', NOW() + INTERVAL '30 days', 'Test Venue 1', 500, 500, 'ON_SALE', NOW() - INTERVAL '1 day', NOW()),
    ('Load Test Event 2', NOW() + INTERVAL '45 days', 'Test Venue 2', 300, 300, 'ON_SALE', NOW() - INTERVAL '1 day', NOW()),
    ('Load Test Event 3', NOW() + INTERVAL '60 days', 'Test Venue 3', 400, 400, 'ON_SALE', NOW() - INTERVAL '1 day', NOW()),
    ('Load Test Event 4', NOW() + INTERVAL '75 days', 'Test Venue 4', 200, 200, 'ON_SALE', NOW() - INTERVAL '1 day', NOW()),
    ('Load Test Event 5', NOW() + INTERVAL '90 days', 'Test Venue 5', 600, 600, 'ON_SALE', NOW() - INTERVAL '1 day', NOW())
ON CONFLICT DO NOTHING;

-- Create seats for Event 1 (500 seats)
-- Section A: Rows 1-10, Seats 1-10 (100 seats - REGULAR)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    1,
    'A' || row_num,
    'A',
    row_num::text,
    'REGULAR',
    50.00,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 100) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Section B: VIP seats (50 seats)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    1,
    'VIP-' || row_num,
    'VIP',
    'VIP',
    'VIP',
    150.00,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 50) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Section C: Premium seats (50 seats)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    1,
    'PREM-' || row_num,
    'PREMIUM',
    'PREM',
    'PREMIUM',
    100.00,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 50) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Section D: Regular seats (300 seats)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    1,
    'D' || row_num,
    'D',
    ((row_num - 1) / 30 + 1)::text,
    'REGULAR',
    50.00,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 300) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Create seats for Event 2 (300 seats)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    2,
    'A' || row_num,
    'A',
    ((row_num - 1) / 30 + 1)::text,
    CASE
        WHEN row_num <= 20 THEN 'VIP'
        WHEN row_num <= 50 THEN 'PREMIUM'
        ELSE 'REGULAR'
    END,
    CASE
        WHEN row_num <= 20 THEN 150.00
        WHEN row_num <= 50 THEN 100.00
        ELSE 75.00
    END,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 300) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Create seats for Event 3 (400 seats)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    3,
    'SEAT-' || row_num,
    'GENERAL',
    ((row_num - 1) / 40 + 1)::text,
    'REGULAR',
    60.00,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 400) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Create seats for Event 4 (200 seats - smaller venue)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    4,
    'S' || row_num,
    CASE
        WHEN row_num <= 50 THEN 'VIP'
        ELSE 'REGULAR'
    END,
    ((row_num - 1) / 20 + 1)::text,
    CASE
        WHEN row_num <= 50 THEN 'VIP'
        ELSE 'REGULAR'
    END,
    CASE
        WHEN row_num <= 50 THEN 200.00
        ELSE 80.00
    END,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 200) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Create seats for Event 5 (600 seats - large venue)
INSERT INTO seats (event_id, seat_number, section, row_number, seat_type, price, status, created_at)
SELECT
    5,
    'L' || row_num,
    CASE
        WHEN row_num <= 100 THEN 'A'
        WHEN row_num <= 200 THEN 'B'
        WHEN row_num <= 300 THEN 'C'
        WHEN row_num <= 400 THEN 'D'
        WHEN row_num <= 500 THEN 'E'
        ELSE 'F'
    END,
    ((row_num - 1) / 30 + 1)::text,
    CASE
        WHEN row_num <= 30 THEN 'VIP'
        WHEN row_num <= 100 THEN 'PREMIUM'
        ELSE 'REGULAR'
    END,
    CASE
        WHEN row_num <= 30 THEN 180.00
        WHEN row_num <= 100 THEN 120.00
        ELSE 65.00
    END,
    'AVAILABLE',
    NOW()
FROM generate_series(1, 600) AS row_num
ON CONFLICT (event_id, seat_number) DO NOTHING;

-- Verify data
SELECT
    e.event_id,
    e.event_name,
    e.total_seats,
    COUNT(s.seat_id) as seats_created,
    COUNT(CASE WHEN s.status = 'AVAILABLE' THEN 1 END) as available_seats,
    COUNT(CASE WHEN s.seat_type = 'VIP' THEN 1 END) as vip_seats,
    COUNT(CASE WHEN s.seat_type = 'PREMIUM' THEN 1 END) as premium_seats,
    COUNT(CASE WHEN s.seat_type = 'REGULAR' THEN 1 END) as regular_seats
FROM events e
LEFT JOIN seats s ON e.event_id = s.event_id
WHERE e.event_name LIKE 'Load Test Event%'
GROUP BY e.event_id, e.event_name, e.total_seats
ORDER BY e.event_id;
