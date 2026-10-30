package com.library.service;

import com.library.dto.book.BookSummaryResponse;
import com.library.dto.category.CategoryResponse;
import com.library.dto.dashboard.AdminDashboardMetricsResponse;
import com.library.dto.dashboard.AuthorSummaryDto;
import com.library.dto.dashboard.UserDashboardResponse;
import com.library.entity.*;
import com.library.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final DownloadRepository downloadRepository;
    private final UserRepository userRepository;
    private final BookRequestRepository bookRequestRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final UserRestrictionRepository userRestrictionRepository;
    private final BookService bookService;
    private final CategoryService categoryService;

    public DashboardService(BookRepository bookRepository,
                            CategoryRepository categoryRepository,
                            ReadingHistoryRepository readingHistoryRepository,
                            DownloadRepository downloadRepository,
                            UserRepository userRepository,
                            BookRequestRepository bookRequestRepository,
                            AccessRequestRepository accessRequestRepository,
                            UserRestrictionRepository userRestrictionRepository,
                            BookService bookService,
                            CategoryService categoryService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.downloadRepository = downloadRepository;
        this.userRepository = userRepository;
        this.bookRequestRepository = bookRequestRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.userRestrictionRepository = userRestrictionRepository;
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    /**
     * Builds the complete dashboard response mapping directly to the attached UI/UX design:
     * 1. Previous Reading Carousel (cards with progress bar, page number)
     * 2. Subjects Section (Categories with book counter chips & icons)
     * 3. New Books (horizontal book cover row)
     * 4. Popular Books (secondary grid)
     * 5. Writers and Authors (avatars, names, book counters)
     */
    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(Long userId) {
        UserDashboardResponse dashboard = new UserDashboardResponse();

        // 1. Previous Reading
        if (userId != null) {
            Page<ReadingHistory> historyPage = readingHistoryRepository.findByUserIdOrderByLastReadAtDesc(userId, PageRequest.of(0, 6));
            List<BookSummaryResponse> readingList = historyPage.getContent().stream().map(h -> {
                BookSummaryResponse dto = bookService.mapToSummary(h.getBook());
                dto.setReadingProgress(h.getProgress());
                dto.setCurrentPage(h.getCurrentPage());
                return dto;
            }).collect(Collectors.toList());
            dashboard.setPreviousReading(readingList);
        }

        // 2. Subjects Section (Science, Arts, Commerce, Design, Cooking, Others)
        List<CategoryResponse> subjects = categoryService.getAllActiveCategories();
        dashboard.setSubjects(subjects);

        // 3. New Books
        Page<Book> newBooksPage = bookRepository.findByStatus(
                BookStatus.AVAILABLE,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        dashboard.setNewBooks(newBooksPage.getContent().stream().map(bookService::mapToSummary).collect(Collectors.toList()));

        // 4. Popular Books
        Page<Book> popularPage = bookRepository.findPopularBooks(BookStatus.AVAILABLE, PageRequest.of(0, 8));
        dashboard.setPopularBooks(popularPage.getContent().stream().map(bookService::mapToSummary).collect(Collectors.toList()));

        // 5. Writers and Authors
        List<AuthorSummaryDto> authors = new ArrayList<>();
        authors.add(new AuthorSummaryDto("Austin Kleon", 76, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"));
        authors.add(new AuthorSummaryDto("Mark Manson", 33, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80"));
        authors.add(new AuthorSummaryDto("Agatha Christie", 12, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80"));
        authors.add(new AuthorSummaryDto("Joseph Murphy", 110, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"));
        authors.add(new AuthorSummaryDto("Subhash Lakhotia", 20, "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80"));
        authors.add(new AuthorSummaryDto("Arthur Conan Doyle", 51, "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=150&q=80"));
        dashboard.setWritersAndAuthors(authors);

        return dashboard;
    }

    /**
     * Builds the metrics summary for the Administrator Dashboard.
     */
    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getAdminDashboardMetrics() {
        AdminDashboardMetricsResponse metrics = new AdminDashboardMetricsResponse();
        metrics.setTotalUsers(userRepository.count());
        metrics.setActiveUsers(userRepository.countByStatus(UserStatus.ACTIVE));
        metrics.setSuspendedUsers(userRepository.countByStatus(UserStatus.SUSPENDED));
        metrics.setTotalBooks(bookRepository.count());
        metrics.setAvailableBooks(bookRepository.countByStatus(BookStatus.AVAILABLE));
        metrics.setRestrictedBooks(bookRepository.countByStatus(BookStatus.RESTRICTED));
        metrics.setTotalDownloads(downloadRepository.count());
        metrics.setPendingBookRequests(bookRequestRepository.countByStatus(RequestStatus.PENDING));
        metrics.setPendingAccessRequests(accessRequestRepository.countByStatus(AccessStatus.PENDING));
        metrics.setActiveRestrictions(userRestrictionRepository.countByIsActiveTrue());
        return metrics;
    }
}

