package com.library.service;

import com.library.dto.book.*;
import com.library.dto.common.PagedResponse;
import com.library.entity.*;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookTypeRepository bookTypeRepository;
    private final UserRepository userRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final DownloadRepository downloadRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PermissionService permissionService;
    private final StorageService storageService;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       BookTypeRepository bookTypeRepository,
                       UserRepository userRepository,
                       ReadingHistoryRepository readingHistoryRepository,
                       DownloadRepository downloadRepository,
                       ActivityLogRepository activityLogRepository,
                       PermissionService permissionService,
                       StorageService storageService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.userRepository = userRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.downloadRepository = downloadRepository;
        this.activityLogRepository = activityLogRepository;
        this.permissionService = permissionService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookSummaryResponse> getBooks(Long categoryId, Long typeId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Book> booksPage;
        if (categoryId != null && typeId != null) {
            booksPage = bookRepository.findByCategoryIdAndTypeIdAndStatus(categoryId, typeId, BookStatus.AVAILABLE, pageable);
        } else if (categoryId != null) {
            booksPage = bookRepository.findByCategoryIdAndStatus(categoryId, BookStatus.AVAILABLE, pageable);
        } else if (typeId != null) {
            booksPage = bookRepository.findByTypeIdAndStatus(typeId, BookStatus.AVAILABLE, pageable);
        } else {
            booksPage = bookRepository.findByStatus(BookStatus.AVAILABLE, pageable);
        }

        List<BookSummaryResponse> content = booksPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return PagedResponse.of(booksPage, content);
    }

    @Transactional
    public BookDetailResponse getBookById(Long id, Long currentUserId) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        // Increment view count
        book.setViewCount(book.getViewCount() + 1);
        bookRepository.save(book);

        BookDetailResponse response = new BookDetailResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setCategoryId(book.getCategory().getId());
        response.setCategoryName(book.getCategory().getName());
        response.setTypeId(book.getType().getId());
        response.setTypeName(book.getType().getName());
        response.setCoverPath(book.getCoverPath());
        response.setStatus(book.getStatus());
        response.setTotalPages(book.getTotalPages());
        response.setFileSizeBytes(book.getFileSizeBytes());
        response.setDownloadCount(book.getDownloadCount());
        response.setViewCount(book.getViewCount());
        response.setIsFeatured(book.getIsFeatured());
        response.setCreatedAt(book.getCreatedAt());

        if (currentUserId != null) {
            userRepository.findById(currentUserId).ifPresent(user -> {
                response.setCanRead(permissionService.canUserRead(user, book));
                response.setCanDownload(permissionService.canUserDownload(user, book));
            });
        }

        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookSummaryResponse> searchBooks(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.searchBooks(query, BookStatus.AVAILABLE, pageable);

        List<BookSummaryResponse> content = booksPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return PagedResponse.of(booksPage, content);
    }

    @Transactional(readOnly = true)
    public BookSuggestionResponse getSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return new BookSuggestionResponse();
        }
        Pageable limit = PageRequest.of(0, 5);
        List<String> titles = bookRepository.findTitleSuggestions(query.trim(), limit);
        List<String> authors = bookRepository.findAuthorSuggestions(query.trim(), limit);
        return new BookSuggestionResponse(titles, authors);
    }

    @Transactional
    public BookAccessUrlResponse requestReadAccess(Long bookId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Enforce centralized permission hierarchy
        permissionService.validateReadPermission(user, book);

        // Update or initialize reading history
        ReadingHistory history = readingHistoryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> new ReadingHistory(user, book, 1, BigDecimal.ZERO));
        history.setLastReadAt(OffsetDateTime.now());
        readingHistoryRepository.save(history);

        // Audit log
        activityLogRepository.save(new ActivityLog(user, "BOOK_READ_SESSION_STARTED", "BOOK", bookId,
                "{\"title\": \"" + book.getTitle() + "\"}", null));

        String signedUrl = storageService.generateProtectedAccessUrl(book.getPdfPath(), "READ");
        return new BookAccessUrlResponse(signedUrl, storageService.getSignedUrlDurationSeconds(), book.getTitle(), "READ");
    }

    @Transactional
    public BookAccessUrlResponse requestDownloadAccess(Long bookId, Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Enforce centralized permission hierarchy
        permissionService.validateDownloadPermission(user, book);

        // Record download transaction
        Download download = new Download(user, book, ipAddress);
        downloadRepository.save(download);

        // Increment book metrics
        book.setDownloadCount(book.getDownloadCount() + 1);
        bookRepository.save(book);

        // Audit log
        activityLogRepository.save(new ActivityLog(user, "BOOK_DOWNLOADED", "BOOK", bookId,
                "{\"title\": \"" + book.getTitle() + "\", \"file_size\": " + book.getFileSizeBytes() + "}", ipAddress));

        String signedUrl = storageService.generateProtectedAccessUrl(book.getPdfPath(), "DOWNLOAD");
        return new BookAccessUrlResponse(signedUrl, storageService.getSignedUrlDurationSeconds(), book.getTitle(), "DOWNLOAD");
    }

    // --- Administrator Operations ---

    @Transactional
    public BookDetailResponse createBook(BookCreateRequest request, Long adminUserId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        BookType type = bookTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("BookType", "id", request.getTypeId()));
        User adminUser = userRepository.findById(adminUserId).orElse(null);

        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getDescription(),
                category,
                type,
                request.getPdfPath(),
                request.getCoverPath(),
                request.getStatus(),
                request.getTotalPages(),
                request.getFileSizeBytes(),
                adminUser
        );
        book.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));

        Book saved = bookRepository.save(book);
        activityLogRepository.save(new ActivityLog(adminUser, "BOOK_CREATED", "BOOK", saved.getId(),
                "{\"title\": \"" + saved.getTitle() + "\"}", null));

        return getBookById(saved.getId(), adminUserId);
    }

    @Transactional
    public BookDetailResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getPdfPath() != null) book.setPdfPath(request.getPdfPath());
        if (request.getCoverPath() != null) book.setCoverPath(request.getCoverPath());
        if (request.getStatus() != null) book.setStatus(request.getStatus());
        if (request.getTotalPages() != null) book.setTotalPages(request.getTotalPages());
        if (request.getFileSizeBytes() != null) book.setFileSizeBytes(request.getFileSizeBytes());
        if (request.getIsFeatured() != null) book.setIsFeatured(request.getIsFeatured());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            book.setCategory(category);
        }

        if (request.getTypeId() != null) {
            BookType type = bookTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookType", "id", request.getTypeId()));
            book.setType(type);
        }

        Book saved = bookRepository.save(book);
        return getBookById(saved.getId(), null);
    }

    @Transactional
    public void restrictBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        book.setStatus(BookStatus.RESTRICTED);
        bookRepository.save(book);
    }

    @Transactional
    public void unrestrictBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        // Soft delete strategy
        book.setStatus(BookStatus.ARCHIVED);
        bookRepository.save(book);
    }

    public BookSummaryResponse mapToSummary(Book book) {
        BookSummaryResponse dto = new BookSummaryResponse();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setCategoryId(book.getCategory().getId());
        dto.setCategoryName(book.getCategory().getName());
        dto.setTypeId(book.getType().getId());
        dto.setTypeName(book.getType().getName());
        dto.setCoverPath(book.getCoverPath());
        dto.setStatus(book.getStatus());
        dto.setTotalPages(book.getTotalPages());
        dto.setDownloadCount(book.getDownloadCount());
        dto.setViewCount(book.getViewCount());
        dto.setIsFeatured(book.getIsFeatured());
        return dto;
    }
}

