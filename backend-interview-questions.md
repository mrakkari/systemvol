# Backend Interview Questions & Answers

## 1. Concurrency Control and Optimistic Locking

**Q: Explain your implementation of optimistic locking in the reservation system. Why did you choose this approach over pessimistic locking?**

**A:** I implemented optimistic locking using JPA's `@Version` annotation in the Vol entity:

```java
@Version
@Column(name = "version")
private Long version = 0L;
```

**Why Optimistic Locking:**
- **Scalability**: Flight reservation systems are read-heavy (thousands search, few book)
- **Performance**: No database row locking, allowing concurrent reads
- **Conflict Rarity**: Seat conflicts are uncommon in normal usage
- **Retry Strategy**: Combined with `@Retryable` for automatic conflict resolution

**Implementation Details:**
- `findByIdWithOptimisticLock()` ensures version checking
- `OptimisticLockingFailureException` triggers retry mechanism
- Maximum 3 retry attempts with exponential backoff
- Graceful degradation with user-friendly error messages

This approach provides better scalability for high-concurrency scenarios typical in flight booking systems.

## 2. Retry Mechanism and Error Handling

**Q: Walk through your retry implementation in ReservationService. How do you handle different types of failures?**

**A:** I implemented a sophisticated retry mechanism:

```java
@Retryable(
    retryFor = {OptimisticLockingFailureException.class, ReservationConflictException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
```

**Retry Strategy:**
- **Specific Exceptions**: Only retries concurrency-related failures
- **Exponential Backoff**: 100ms, 200ms, 400ms delays
- **Limited Attempts**: Maximum 3 retries to prevent infinite loops

**Error Classification:**
- **Retryable**: Optimistic locking conflicts, temporary concurrency issues
- **Non-retryable**: Business logic errors (insufficient seats, invalid data)
- **Fatal**: System errors that require immediate attention

**Audit Integration:**
Every attempt (success/failure) is logged asynchronously for complete traceability without affecting performance.

## 3. Asynchronous Audit System

**Q: Explain your audit logging implementation. Why did you make it asynchronous?**

**A:** The audit system uses Spring's `@Async` with custom thread pool configuration:

```java
@EventListener
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleReservationEvent(ReservationEvent event)
```

**Asynchronous Benefits:**
- **Performance**: Doesn't block main reservation flow
- **Resilience**: Audit failures don't affect business operations
- **Scalability**: Separate thread pool handles audit load

**Transaction Isolation:**
- `REQUIRES_NEW` ensures audit has independent transaction
- Audit success/failure doesn't affect main reservation
- Prevents audit rollback from affecting business data

**Event-Driven Architecture:**
- Loose coupling between business logic and audit
- Easy to extend with additional event listeners
- Complete audit trail for compliance requirements

## 4. Caching Strategy Implementation

**Q: Describe your caching implementation. How do you ensure cache consistency?**

**A:** I implemented intelligent caching using Caffeine:

```java
@Cacheable(value = "vol-places", key = "#volId")
public Integer getPlacesDisponibles(UUID volId)

@CacheEvict(value = "vol-places", key = "#volId")
public void evictCache(UUID volId)
```

**Cache Configuration:**
- **TTL**: 10-minute expiration for data freshness
- **Size Limit**: 1000 entries to prevent memory issues
- **Statistics**: Performance monitoring enabled

**Consistency Strategy:**
- **Immediate Eviction**: Cache cleared after successful reservation
- **Key-based Eviction**: Only affected flight data is invalidated
- **Fallback**: Database query if cache miss occurs

**Performance Impact:**
- Reduces database load for frequent seat availability checks
- Improves response times for search operations
- Balances performance with data accuracy

## 5. Custom Exception Handling

**Q: Explain your global exception handling strategy. How do you provide meaningful error responses?**

**A:** I implemented comprehensive exception handling with `@ControllerAdvice`:

**Custom Exception Hierarchy:**
```java
public class PlacesInsuffisantesException extends RuntimeException {
    private final Integer placesDisponibles;
    private final Integer placesDemandees;
}
```

**Structured Error Responses:**
```java
public class ErrorResponse {
    private String code;
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
```

**Exception Categories:**
- **Business Logic**: `PlacesInsuffisantesException`, `VolNotFoundException`
- **Concurrency**: `ReservationConflictException`, `OptimisticLockingFailureException`
- **Validation**: `MethodArgumentNotValidException`, `ConstraintViolationException`
- **System**: Generic `Exception` with sanitized messages

**Client Benefits:**
- Consistent error format across all endpoints
- Actionable error codes for frontend handling
- Detailed information for debugging without exposing internals

## 6. JPA Specifications and Dynamic Querying

**Q: How did you implement flexible flight search using JPA Specifications?**

**A:** I created reusable specification components:

```java
public static Specification<Vol> hasDateDepart(LocalDateTime dateDepart) {
    if (dateDepart == null) return null;
    LocalDate searchDate = dateDepart.toLocalDate();
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(
            root.get("dateDepart").as(LocalDate.class),
            searchDate
        );
}
```

**Dynamic Query Building:**
```java
Specification<Vol> spec = Specification.where(VolSpecification.hasDateDepart(dateDepart))
    .and(VolSpecification.hasDateArrivee(dateArrivee))
    .and(VolSpecification.hasVilleDepart(villeDepart))
    .and(VolSpecification.hasVilleArrivee(villeArrivee));
```

**Advantages:**
- **Composable**: Mix and match search criteria
- **Type Safe**: Compile-time checking of query structure
- **Reusable**: Specifications can be combined across different services
- **Performance**: Only generates SQL for non-null criteria

This approach provides flexible search without SQL injection risks or complex query building logic.

## 7. Entity Design and Relationships

**Q: Explain your entity design choices, particularly the use of embedded objects and relationship mappings.**

**A:** I designed entities with careful consideration of performance and maintainability:

**Embedded Objects:**
```java
@Embedded
@Valid
@NotNull
private Passager passager;
```

**Benefits of Embedding Passager:**
- **Denormalization**: Avoids unnecessary joins for passenger data
- **Validation**: Bean validation works seamlessly
- **Simplicity**: No separate passenger table management
- **Performance**: Single table access for reservation details

**Relationship Design:**
```java
@OneToMany(mappedBy = "vol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Reservation> reservations = new ArrayList<>();
```

**Lazy Loading Strategy:**
- Prevents N+1 query problems
- Loads related data only when needed
- Improves initial query performance

**Audit Fields:**
- `@CreationTimestamp` and `@UpdateTimestamp` for automatic tracking
- Version field for optimistic locking
- Proper indexing considerations for query performance

## 8. Business Logic Implementation

**Q: How did you implement the seat reservation business logic to ensure data integrity?**

**A:** I implemented atomic seat reservation with multiple safety layers:

**Business Logic in Entity:**
```java
public void reservePlaces(Integer nombrePlaces) {
    if (!hasAvailableSeats(nombrePlaces)) {
        throw new IllegalStateException("Pas assez de places disponibles");
    }
    this.placesReservees += nombrePlaces;
}
```

**Service Layer Coordination:**
1. **Optimistic Lock**: Ensures version consistency
2. **Availability Check**: Validates seat availability
3. **Atomic Update**: Single transaction for all changes
4. **Cache Invalidation**: Maintains cache consistency
5. **Audit Logging**: Complete operation tracking

**Data Integrity Measures:**
- **Database Constraints**: Prevent negative values
- **Business Rules**: Capacity validation
- **Transaction Boundaries**: All-or-nothing operations
- **Concurrent Access**: Version-based conflict detection

This multi-layered approach ensures data consistency even under high concurrency.

## 9. Testing Strategy and Implementation

**Q: Describe your testing approach, particularly for concurrent scenarios. How do you test optimistic locking?**

**A:** I implemented comprehensive testing with focus on concurrency:

**Concurrent Testing:**
```java
@RepeatedTest(3)
void should_handle_concurrent_reservations() throws Exception {
    int numberOfRequests = 5;
    int seatsPerRequest = 3;
    // ... concurrent execution logic
}
```

**Testing Strategy:**
- **Integration Tests**: Full HTTP stack with MockMvc
- **Concurrent Scenarios**: Multiple threads attempting simultaneous reservations
- **Edge Cases**: Boundary conditions and error scenarios
- **Database Testing**: H2 in-memory for isolated testing

**Concurrency Validation:**
- **Success Counting**: Verify expected number of successful reservations
- **Conflict Handling**: Ensure proper error responses for conflicts
- **Data Consistency**: Validate final database state
- **Performance**: Measure response times under load

**Test Environment:**
- **H2 Database**: Fast, isolated test execution
- **Mock Dependencies**: Unit test isolation
- **Realistic Data**: Representative test scenarios

## 10. Configuration and Infrastructure

**Q: Explain your Spring Boot configuration choices. How did you configure caching, async processing, and CORS?**

**A:** I implemented comprehensive Spring Boot configuration:

**Caching Configuration:**
```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .recordStats());
    return cacheManager;
}
```

**Async Configuration:**
```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    return executor;
}
```

**CORS Configuration:**
```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:4200")
    .allowedMethods("GET", "POST", "PUT", "DELETE")
    .allowedHeaders("*");
```

**Database Configuration:**
- **SQLite**: Lightweight development database
- **H2**: In-memory testing database
- **Hibernate**: Proper dialect configuration for each database
- **Connection Pooling**: Default HikariCP for production readiness

This configuration provides a production-ready foundation with proper separation of concerns and environment-specific settings.