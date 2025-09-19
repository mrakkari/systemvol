package com.flight.reservation.repository;

import com.flight.reservation.entity.Vol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolRepository extends JpaRepository<Vol, UUID>, JpaSpecificationExecutor<Vol> {
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT v FROM Vol v WHERE v.id = :id")
    Optional<Vol> findByIdWithOptimisticLock(@Param("id") UUID id);
    
    @Query("SELECT v.placesReservees FROM Vol v WHERE v.id = :id")
    Optional<Integer> findPlacesReserveesByVolId(@Param("id") UUID id);
}
/**
 * We use OPTIMISTIC locking in this system instead of PESSIMISTIC locking.
 *
 * Justification:
 * - The flight reservation system is a READ-heavy application: thousands of users search
 *   and view flights simultaneously, but only a smaller fraction actually book.
 * - Optimistic locking scales much better in this scenario, since it does not block rows
 *   in the database. Users can freely read flight data without waiting on locks.
 * - Conflicts (e.g., multiple users reserving the last seats at the same time) are rare.
 *   When they happen, the @Version field detects the conflict and throws an
 *   OptimisticLockException, which can be safely handled with a retry.
 * - Pessimistic locking, while safe, would introduce blocking and reduce concurrency,
 *   hurting performance during peak traffic (e.g., sales or promotions).
 *
 * In short: Optimistic locking provides better scalability and performance
 * for our high-concurrency, read-dominated use case of flight reservations.
 */
