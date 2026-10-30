package com.library.repository;

import com.library.entity.AccessRequest;
import com.library.entity.AccessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    Page<AccessRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AccessRequest> findByStatusOrderByCreatedAtDesc(AccessStatus status, Pageable pageable);

    Page<AccessRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<AccessRequest> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, AccessStatus status);

    long countByStatus(AccessStatus status);
}

