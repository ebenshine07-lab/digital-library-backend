package com.library.repository;

import com.library.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    Optional<ReadingHistory> findByUserIdAndBookId(Long userId, Long bookId);

    Page<ReadingHistory> findByUserIdOrderByLastReadAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ReadingHistory r WHERE r.user.id = :userId AND r.completed = true")
    long countCompletedByUserId(@Param("userId") Long userId);
}

