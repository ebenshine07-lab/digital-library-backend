package com.library.service;

import com.library.dto.common.PagedResponse;
import com.library.dto.history.DownloadHistoryResponse;
import com.library.dto.history.ReadingHistoryResponse;
import com.library.dto.history.ReadingProgressUpdateRequest;
import com.library.entity.Book;
import com.library.entity.Download;
import com.library.entity.ReadingHistory;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.DownloadRepository;
import com.library.repository.ReadingHistoryRepository;
import com.library.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final DownloadRepository downloadRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public HistoryService(ReadingHistoryRepository readingHistoryRepository,
                          DownloadRepository downloadRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.downloadRepository = downloadRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReadingHistoryResponse> getUserReadingHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReadingHistory> historyPage = readingHistoryRepository.findByUserIdOrderByLastReadAtDesc(userId, pageable);

        List<ReadingHistoryResponse> content = historyPage.getContent().stream()
                .map(this::mapToReadingResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(historyPage, content);
    }

    @Transactional
    public ReadingHistoryResponse updateReadingProgress(Long userId, Long bookId, ReadingProgressUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        ReadingHistory history = readingHistoryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> new ReadingHistory(user, book, 1, BigDecimal.ZERO));

        history.setCurrentPage(request.getCurrentPage());

        // Calculate progress percentage automatically if not provided or to ensure accuracy
        if (request.getProgress() != null) {
            history.setProgress(request.getProgress());
        } else if (book.getTotalPages() > 0) {
            BigDecimal progress = BigDecimal.valueOf(request.getCurrentPage())
                    .divide(BigDecimal.valueOf(book.getTotalPages()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            history.setProgress(progress.min(BigDecimal.valueOf(100.00)));
        }

        history.setLastReadAt(OffsetDateTime.now());
        ReadingHistory saved = readingHistoryRepository.save(history);

        return mapToReadingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DownloadHistoryResponse> getUserDownloads(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Download> downloadsPage = downloadRepository.findByUserIdOrderByDownloadedAtDesc(userId, pageable);

        List<DownloadHistoryResponse> content = downloadsPage.getContent().stream()
                .map(this::mapToDownloadResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(downloadsPage, content);
    }

    private ReadingHistoryResponse mapToReadingResponse(ReadingHistory history) {
        ReadingHistoryResponse dto = new ReadingHistoryResponse();
        dto.setId(history.getId());
        dto.setBookId(history.getBook().getId());
        dto.setBookTitle(history.getBook().getTitle());
        dto.setBookAuthor(history.getBook().getAuthor());
        dto.setCoverPath(history.getBook().getCoverPath());
        dto.setCurrentPage(history.getCurrentPage());
        dto.setTotalPages(history.getBook().getTotalPages());
        dto.setProgress(history.getProgress());
        dto.setCompleted(history.getCompleted());
        dto.setStartedAt(history.getStartedAt());
        dto.setLastReadAt(history.getLastReadAt());
        return dto;
    }

    private DownloadHistoryResponse mapToDownloadResponse(Download download) {
        DownloadHistoryResponse dto = new DownloadHistoryResponse();
        dto.setId(download.getId());
        dto.setBookId(download.getBook().getId());
        dto.setBookTitle(download.getBook().getTitle());
        dto.setBookAuthor(download.getBook().getAuthor());
        dto.setCoverPath(download.getBook().getCoverPath());
        dto.setDownloadedAt(download.getDownloadedAt());
        return dto;
    }
}

