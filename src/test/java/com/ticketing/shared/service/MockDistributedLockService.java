package com.ticketing.shared.service;

import io.quarkus.logging.Log;
import io.quarkus.test.Mock;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of DistributedLockService for testing.
 * Uses in-memory ConcurrentHashMap instead of Redis.
 * This allows tests to run without external dependencies.
 */
@Mock
@Alternative
@Priority(1)
@ApplicationScoped
public class MockDistributedLockService extends DistributedLockService {

    private final Map<String, String> locks = new ConcurrentHashMap<>();
    private final String lockPrefix = "seat:lock:";

    public MockDistributedLockService() {
        super(null); // No Redis needed for mock
    }

    @Override
    public boolean tryLock(String resourceKey, String lockValue) {
        return tryLock(resourceKey, lockValue, Duration.ofSeconds(30));
    }

    @Override
    public boolean tryLock(String resourceKey, String lockValue, Duration timeout) {
        String lockKey = lockPrefix + resourceKey;

        // Use putIfAbsent to simulate Redis NX behavior (only set if not exists)
        String existingValue = locks.putIfAbsent(lockKey, lockValue);

        boolean acquired = (existingValue == null);

        if (acquired) {
            Log.debugf("Mock lock acquired for key: %s with value: %s", lockKey, lockValue);
        } else {
            Log.debugf("Mock lock NOT acquired for key: %s (already held by: %s)", lockKey, existingValue);
        }

        return acquired;
    }

    @Override
    public List<String> tryLockMultiple(List<String> resourceKeys, String lockValue) {
        // Sort to prevent deadlock
        List<String> sortedKeys = new ArrayList<>(resourceKeys);
        Collections.sort(sortedKeys);

        List<String> acquiredLocks = new ArrayList<>();

        try {
            for (String resourceKey : sortedKeys) {
                if (!tryLock(resourceKey, lockValue)) {
                    // Failed to acquire all locks, release what we have
                    releaseLocks(acquiredLocks, lockValue);
                    return Collections.emptyList();
                }
                acquiredLocks.add(resourceKey);
            }
            return acquiredLocks;
        } catch (Exception e) {
            Log.errorf(e, "Error acquiring multiple locks");
            releaseLocks(acquiredLocks, lockValue);
            return Collections.emptyList();
        }
    }

    @Override
    public void releaseLock(String resourceKey, String lockValue) {
        String lockKey = lockPrefix + resourceKey;

        // Only release if the lock value matches (simulate Lua script behavior)
        boolean removed = locks.remove(lockKey, lockValue);

        if (removed) {
            Log.debugf("Mock lock released for key: %s", lockKey);
        } else {
            Log.warnf("Mock lock not released (value mismatch or not held) for key: %s", lockKey);
        }
    }

    @Override
    public void releaseLocks(List<String> resourceKeys, String lockValue) {
        for (String resourceKey : resourceKeys) {
            releaseLock(resourceKey, lockValue);
        }
    }

    @Override
    public String generateLockValue() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String buildSeatLockKey(Long eventId, String seatNumber) {
        return eventId + ":" + seatNumber;
    }

    /**
     * Test helper method to clear all locks between tests
     */
    public void clearAllLocks() {
        locks.clear();
        Log.debug("All mock locks cleared");
    }

    /**
     * Test helper method to check if a lock is held
     */
    public boolean isLocked(String resourceKey) {
        String lockKey = lockPrefix + resourceKey;
        return locks.containsKey(lockKey);
    }
}
