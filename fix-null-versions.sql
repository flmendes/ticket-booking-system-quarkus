-- SQL Script to fix NULL version fields in existing data
-- This updates all entities that have NULL in the version column
-- Run this script BEFORE restarting the application

-- Fix seats table
UPDATE seats
SET version = 0
WHERE version IS NULL;

-- Fix events table
UPDATE events
SET version = 0
WHERE version IS NULL;

-- Verify the fix
SELECT 'seats' as table_name, COUNT(*) as fixed_rows
FROM seats
WHERE version = 0

UNION ALL

SELECT 'events' as table_name, COUNT(*) as fixed_rows
FROM events
WHERE version = 0;

-- Show summary
SELECT
    'seats' as table_name,
    COUNT(*) as total_rows,
    COUNT(CASE WHEN version IS NULL THEN 1 END) as null_versions,
    COUNT(CASE WHEN version = 0 THEN 1 END) as zero_versions
FROM seats

UNION ALL

SELECT
    'events' as table_name,
    COUNT(*) as total_rows,
    COUNT(CASE WHEN version IS NULL THEN 1 END) as null_versions,
    COUNT(CASE WHEN version = 0 THEN 1 END) as zero_versions
FROM events;
