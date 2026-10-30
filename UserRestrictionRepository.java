package com.library.repository;

import com.library.entity.RestrictionType;
import com.library.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {

    List<UserRestriction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT r FROM UserRestriction r WHERE r.user.id = :userId " +
           "AND r.isActive = true " +
           "AND r.startsAt <= :now " +
           "AND r.expiresAt > :now")
    List<UserRestriction> findActiveRestrictions(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    @Query("SELECT COUNT(r) > 0 FROM UserRestriction r WHERE r.user.id = :userId " +
           "AND r.isActive = true " +
           "AND r.startsAt <= :now " +
           "AND r.expiresAt > :now " +
           "AND (r.restrictionType = :restrictionType OR r.restrictionType = 'SUSPEND_ALL')")
    boolean hasActiveRestriction(@Param("userId") Long userId,
                                 @Param("restrictionType") RestrictionType restrictionType,
                                 @Param("now") OffsetDateTime now);

    long countByIsActiveTrue();
}

