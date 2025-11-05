package com.ticketing.shared.service;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DistributedLockService {

    private final RedisDataSource ds;

    @ConfigProperty(name = "ticketing.lock.timeout-seconds", defaultValue = "30")
    int lockTimeoutSeconds;

    @ConfigProperty(name = "ticketing.lock.prefix", defaultValue = "seat:lock:")
    String lockPrefix;

    public DistributedLockService(RedisDataSource ds) {
        this.ds = ds;
        ValueCommands<String, String> commands = ds.value(String.class, String.class);
    }

    /**
     * Try to acquire a lock for a specific resource
     *
     * @param resourceKey The resource to lock (e.g., "eventId:seatNumber")
     * @param lockValue   Unique identifier for this lock acquisition
     * @return true if lock was acquired, false otherwise
     */
    public boolean tryLock(String resourceKey, String lockValue) {
        return tryLock(resourceKey, lockValue, Duration.ofSeconds(lockTimeoutSeconds));
    }

    /**
     * Try to acquire a lock with custom timeout
     */
    public boolean tryLock(String resourceKey, String lockValue, Duration timeout) {
        String lockKey = lockPrefix + resourceKey;

        try {
            // Use a single atomic SET with NX and EX to set the value and expiration
            // Redis command: SET key value NX EX seconds
            Object res = ds.execute("SET", lockKey, lockValue, "NX", "EX", String.valueOf(timeout.getSeconds()));

            // The Redis SET with NX returns the string "OK" when the key was set, or null otherwise
            if (res instanceof String && "OK".equals(res)) {
                Log.debugf("Lock acquired for key: %s with value: %s", lockKey, lockValue);
                return true;
            }

            Log.debugf("Failed to acquire lock for key: %s", lockKey);
            return false;
        } catch (Exception e) {
            Log.errorf(e, "Error acquiring lock for key: %s", lockKey);
            return false;
        }
    }

    /**
     * Try to acquire locks for multiple resources
     * Uses sorted order to prevent deadlocks
     *
     * @param resourceKeys List of resources to lock
     * @param lockValue    Unique identifier for this lock acquisition
     * @return List of successfully acquired lock keys, empty if any lock fails
     */
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

    /**
     * Release a lock
     * Uses Lua script to ensure atomic check-and-delete
     */
    public void releaseLock(String resourceKey, String lockValue) {
        String lockKey = lockPrefix + resourceKey;

        try {
            // Lua script to atomically check value and delete
            String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";

            // Execute Lua script via the data source
            Object execResult = ds.execute("EVAL", luaScript, "1", lockKey, lockValue);

            Long result = null;
            if (execResult instanceof Long) {
                result = (Long) execResult;
            } else if (execResult instanceof Integer) {
                result = ((Integer) execResult).longValue();
            } else if (execResult instanceof String) {
                try {
                    result = Long.valueOf((String) execResult);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }

            if (result != null && result > 0) {
                Log.debugf("Lock released for key: %s", lockKey);
            } else {
                Log.warnf(
                    "Lock not released (value mismatch or already expired) for key: %s",
                    lockKey
                );
            }
        } catch (Exception e) {
            Log.errorf(e, "Error releasing lock for key: %s", lockKey);
        }
    }

    /**
     * Release multiple locks
     */
    public void releaseLocks(List<String> resourceKeys, String lockValue) {
        for (String resourceKey : resourceKeys) {
            releaseLock(resourceKey, lockValue);
        }
    }

    /**
     * Generate a unique lock value
     */
    public String generateLockValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * Build lock key for event and seat
     */
    public String buildSeatLockKey(Long eventId, String seatNumber) {
        return eventId + ":" + seatNumber;
    }
}
