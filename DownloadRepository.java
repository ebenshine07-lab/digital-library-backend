package com.library.repository;

import com.library.entity.Download;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    Page<Download> findByUserIdOrderByDownloadedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByBookId(Long bookId);
}

