package com.ticketing.shared.service;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
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

    @ConfigProperty(name = "ticketing.lock.acquire-retries", defaultValue = "3")
    int acquireRetries;

    @ConfigProperty(name = "ticketing.lock.retry-delay-ms", defaultValue = "30")
    long retryDelayMs;

    @ConfigProperty(name = "ticketing.lock.force-expire-on-stale", defaultValue = "true")
    boolean forceExpireOnStale;

    public DistributedLockService(RedisDataSource ds) {
        this.ds = ds;
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

        long seconds = Math.max(1, timeout.getSeconds());
        int attempts = Math.max(1, acquireRetries);
        for (int i = 0; i < attempts; i++) {
            try {
                Object res = ds.execute(
                    "SET",
                    lockKey,
                    lockValue,
                    "NX",
                    "EX",
                    String.valueOf(seconds)
                );

                // Success if Redis acknowledged with OK
                if (res != null && "OK".equalsIgnoreCase(res.toString())) {
                    Log.debugf("Lock acquired for key: %s with value: %s", lockKey, lockValue);
                    return true;
                }

                // Some clients may return non-String response; verify via GET when non-null
                if (res != null) {
                    Object getRes = ds.execute("GET", lockKey);
                    if (getRes != null && lockValue.equals(getRes.toString())) {
                        Log.debugf("Lock acquired (verified by GET) for key: %s", lockKey);
                        return true;
                    }
                }

                // If not last attempt, backoff and retry
                if (i < attempts - 1) {
                    sleepQuietly(retryDelayMs);
                    continue;
                }

                // Final failure: diagnostics and optional self-healing
                Object holder = ds.execute("GET", lockKey);
                Object ttlObj = ds.execute("PTTL", lockKey);
                long pttl = parseLong(ttlObj, Long.MIN_VALUE);
                Log.debugf(
                    "Failed to acquire lock for key: %s (holder=%s, pttl=%sms)",
                    lockKey,
                    holder,
                    ttlObj
                );

                // If no TTL is set (-1), optionally force an expiration to avoid permanent stale locks
                if (forceExpireOnStale && pttl == -1L) {
                    Object expRes = ds.execute("EXPIRE", lockKey, String.valueOf(seconds));
                    Log.warnf(
                        "Detected stale lock without TTL for key: %s. Forced EXPIRE %ds (result=%s)",
                        lockKey,
                        seconds,
                        expRes
                    );
                }

                return false;
            } catch (Exception e) {
                Log.errorf(e, "Error acquiring lock for key: %s", lockKey);
                return false;
            }
        }
        // Should not reach here
        return false;
    }

    private static long parseLong(Object obj, long def) {
        if (obj == null) return def;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return def;
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
            String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";

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

    private static void sleepQuietly(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
