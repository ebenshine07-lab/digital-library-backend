package com.library.repository;

import com.library.entity.BookPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookPermissionRepository extends JpaRepository<BookPermission, Long> {

    Optional<BookPermission> findByUserIdAndBookId(Long userId, Long bookId);

    List<BookPermission> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT bp FROM BookPermission bp WHERE bp.user.id = :userId " +
           "AND bp.book.id = :bookId " +
           "AND bp.startsAt <= :now " +
           "AND (bp.expiresAt IS NULL OR bp.expiresAt > :now)")
    Optional<BookPermission> findActivePermission(@Param("userId") Long userId,
                                                 @Param("bookId") Long bookId,
                                                 @Param("now") OffsetDateTime now);
}

