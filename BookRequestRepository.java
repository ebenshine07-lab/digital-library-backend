package com.library.repository;

import com.library.entity.BookRequest;
import com.library.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {

    Page<BookRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<BookRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    Page<BookRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(RequestStatus status);
}

