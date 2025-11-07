# Bug Fix: Test Failure with 402 Payment Required

## Problem Description

Integration tests were failing intermittently in GitHub Actions with:
```
Error:  Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
Error:  com.ticketing.it.BookingFlowTest.should_reserve_two_seats_and_confirm_booking
Expected status code <200> but was <402>
```

## Root Cause Analysis

### Issue: Non-Deterministic Payment Service

The `PaymentService` class had a **95% success rate** with random failures:

**Location**: `src/main/java/com/ticketing/booking/service/PaymentService.java:32`

```java
// PROBLEMATIC CODE
public PaymentResponse processPayment(PaymentRequest paymentRequest) {
    // Mock: 95% success rate for demo purposes
    boolean isSuccess = Math.random() < 0.95;  // ❌ 5% RANDOM FAILURE!

    if (isSuccess) {
        // Return success
    } else {
        // Return failure
    }
}
```

### Failure Flow

1. **Random Failure (5% chance)**:
   - `PaymentService.processPayment()` returns `success = false`

2. **Exception Thrown**:
   - `BookingService.confirmBooking()` checks payment response
   - Throws `PaymentFailedException` when payment fails

3. **HTTP 402 Response**:
   - `PaymentFailedExceptionMapper` catches the exception
   - Returns `HTTP 402 PAYMENT_REQUIRED` status code

### Why Tests Were Flaky

- **Local Development**: Tests would mostly pass (95% of the time)
- **GitHub Actions**: Running multiple builds increased chance of hitting the 5% failure
- **Non-Deterministic**: Test results depended on random number generation

## Solution

Created a **Mock Payment Service** for tests that **always succeeds**.

### Implementation

**File**: `src/test/java/com/ticketing/booking/service/MockPaymentService.java`

```java
@Mock
@Alternative
@Priority(1)
@ApplicationScoped
public class MockPaymentService extends PaymentService {

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        Log.infof("Mock: Processing payment: %s", paymentRequest);

        // ✅ Always succeed in tests for deterministic behavior
        String paymentId = "PAY-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PaymentResponse.builder()
            .paymentId(paymentId)
            .success(true)
            .status("SUCCESS")
            .processedAt(LocalDateTime.now())
            .build();
    }
}
```

### How It Works

1. **Quarkus CDI Alternative**:
   - `@Mock` annotation marks this as a test mock
   - `@Alternative` makes it override the real service
   - `@Priority(1)` ensures it takes precedence during tests

2. **Automatic Injection**:
   - Quarkus automatically uses `MockPaymentService` in test profile
   - Production code continues using real `PaymentService` with 95% success rate

3. **Deterministic Tests**:
   - All test payments now succeed 100% of the time
   - Tests are reliable and reproducible

## Test Results

### Before Fix
```bash
# Flaky - sometimes passes, sometimes fails
Tests run: 2, Failures: 1, Errors: 0, Skipped: 0 ❌
Expected status code <200> but was <402>
```

### After Fix
```bash
# Always passes
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 ✅
BUILD SUCCESS
```

## Related Files

### Production Code
- `src/main/java/com/ticketing/booking/service/PaymentService.java`
  - Original service with 95% success rate
  - **NO CHANGES** - keeps realistic behavior for demos

- `src/main/java/com/ticketing/shared/exception/handler/PaymentFailedExceptionMapper.java`
  - Maps `PaymentFailedException` to HTTP 402
  - **NO CHANGES**

### Test Code
- `src/test/java/com/ticketing/booking/service/MockPaymentService.java`
  - **NEW**: Mock service for tests
  - Always returns successful payments

- `src/test/java/com/ticketing/it/BookingFlowTest.java`
  - **NO CHANGES** - automatically uses mock

## Testing Strategy

### Unit Tests
- Use `MockPaymentService` for deterministic behavior
- Test booking flow without payment randomness

### Integration Tests
- Tests run with H2 in-memory database
- Mock services for external dependencies (Payment, Redis)
- Fast, reliable, no external dependencies

### Manual Testing / Demos
- Use real `PaymentService` with 95% success rate
- Demonstrates realistic payment failures
- Good for showing error handling

## Benefits

1. **✅ Reliable CI/CD**: Tests no longer fail randomly in GitHub Actions
2. **✅ Fast Feedback**: Developers get consistent test results
3. **✅ Clean Separation**: Test mocks don't affect production code
4. **✅ Realistic Demos**: Production code still shows failure scenarios

## Prevention

To avoid similar issues in the future:

1. **Avoid Randomness in Business Logic**:
   - Use configuration/feature flags instead of `Math.random()`
   - Make randomness controllable via environment variables

2. **Mock External Dependencies**:
   - Always mock services with unpredictable behavior
   - Use `@Mock` and `@Alternative` for test overrides

3. **Test Determinism**:
   - Tests should produce same results every time
   - Use fixed seeds for random number generators if needed

## Related Fixes

This fix complements:
- `MockDistributedLockService.java` - Mock Redis for tests
- `src/test/resources/application.yml` - H2 database for tests
- Test data population with `import.sql`

All external dependencies are now mocked for reliable testing.

## Version

- **Fixed In**: 1.0.0-SNAPSHOT
- **Date**: 2025-11-07
- **Related Issue**: GitHub Actions test failures with HTTP 402
