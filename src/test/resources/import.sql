-- Test Data Import Script
-- This file is automatically executed by Hibernate after schema creation
-- Used for integration and unit tests
-- Compatible with H2 in PostgreSQL mode

-- Note: DELETE statements not needed as we use drop-and-create strategy

-- Insert Test Events
-- Event 1: Concert - for general testing
INSERT INTO events (event_id, event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, version, created_at)
VALUES (1, 'Rock Concert 2025', DATEADD('DAY', 30, CURRENT_TIMESTAMP), 'Stadium Arena', 20, 20, 'ON_SALE', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 0, CURRENT_TIMESTAMP);

-- Event 2: Theater Show - for testing different event types
INSERT INTO events (event_id, event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, version, created_at)
VALUES (2, 'Broadway Show', DATEADD('DAY', 45, CURRENT_TIMESTAMP), 'City Theater', 15, 15, 'ON_SALE', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 0, CURRENT_TIMESTAMP);

-- Event 3: Sports Event - for testing edge cases
INSERT INTO events (event_id, event_name, event_date, venue_name, total_seats, available_seats, status, sale_start_time, version, created_at)
VALUES (3, 'Championship Game', DATEADD('DAY', 60, CURRENT_TIMESTAMP), 'Sports Complex', 10, 10, 'ON_SALE', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 0, CURRENT_TIMESTAMP);

-- Insert Test Seats for Event 1 (Rock Concert)
-- VIP Seats (5)
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (1, 1, 'A1', 'VIP', 'A', 'VIP', 299.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (2, 1, 'A2', 'VIP', 'A', 'VIP', 299.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (3, 1, 'A3', 'VIP', 'A', 'VIP', 299.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (4, 1, 'A4', 'VIP', 'A', 'VIP', 299.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (5, 1, 'A5', 'VIP', 'A', 'VIP', 299.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

-- Premium Seats (10)
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (6, 1, 'B1', 'Premium', 'B', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (7, 1, 'B2', 'Premium', 'B', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (8, 1, 'B3', 'Premium', 'B', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (9, 1, 'B4', 'Premium', 'B', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (10, 1, 'B5', 'Premium', 'B', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (11, 1, 'C1', 'Premium', 'C', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (12, 1, 'C2', 'Premium', 'C', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (13, 1, 'C3', 'Premium', 'C', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (14, 1, 'C4', 'Premium', 'C', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (15, 1, 'C5', 'Premium', 'C', 'PREMIUM', 149.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

-- Regular Seats (5)
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (16, 1, 'D1', 'Regular', 'D', 'REGULAR', 79.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (17, 1, 'D2', 'Regular', 'D', 'REGULAR', 79.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (18, 1, 'D3', 'Regular', 'D', 'REGULAR', 79.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (19, 1, 'D4', 'Regular', 'D', 'REGULAR', 79.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (20, 1, 'D5', 'Regular', 'D', 'REGULAR', 79.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

-- Insert Test Seats for Event 2 (Theater Show) - Smaller set
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (21, 2, 'A1', 'VIP', 'A', 'VIP', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (22, 2, 'A2', 'VIP', 'A', 'VIP', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (23, 2, 'A3', 'VIP', 'A', 'VIP', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (24, 2, 'B1', 'Premium', 'B', 'PREMIUM', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (25, 2, 'B2', 'Premium', 'B', 'PREMIUM', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (26, 2, 'B3', 'Premium', 'B', 'PREMIUM', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (27, 2, 'B4', 'Premium', 'B', 'PREMIUM', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (28, 2, 'B5', 'Premium', 'B', 'PREMIUM', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (29, 2, 'C1', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (30, 2, 'C2', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (31, 2, 'C3', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (32, 2, 'C4', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (33, 2, 'C5', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (34, 2, 'C6', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (35, 2, 'C7', 'Regular', 'C', 'REGULAR', 49.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

-- Insert Test Seats for Event 3 (Sports Event) - Minimal set
INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (36, 3, 'A1', 'VIP', 'A', 'VIP', 399.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (37, 3, 'A2', 'VIP', 'A', 'VIP', 399.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (38, 3, 'B1', 'Premium', 'B', 'PREMIUM', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (39, 3, 'B2', 'Premium', 'B', 'PREMIUM', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (40, 3, 'B3', 'Premium', 'B', 'PREMIUM', 199.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (41, 3, 'C1', 'Regular', 'C', 'REGULAR', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (42, 3, 'C2', 'Regular', 'C', 'REGULAR', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (43, 3, 'C3', 'Regular', 'C', 'REGULAR', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (44, 3, 'C4', 'Regular', 'C', 'REGULAR', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

INSERT INTO seats (seat_id, event_id, seat_number, section, row_number, seat_type, price, status, version, created_at)
VALUES (45, 3, 'C5', 'Regular', 'C', 'REGULAR', 99.99, 'AVAILABLE', 0, CURRENT_TIMESTAMP);

-- Note: Sequences are automatically managed by Hibernate in H2
