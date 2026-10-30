package com.library.service;

import com.library.dto.common.PagedResponse;
import com.library.dto.request.*;
import com.library.entity.*;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final BookRequestRepository bookRequestRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final BookPermissionRepository bookPermissionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PermissionService permissionService;

    public RequestService(BookRequestRepository bookRequestRepository,
                          AccessRequestRepository accessRequestRepository,
                          BookPermissionRepository bookPermissionRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository,
                          ActivityLogRepository activityLogRepository,
                          PermissionService permissionService) {
        this.bookRequestRepository = bookRequestRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.bookPermissionRepository = bookPermissionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.activityLogRepository = activityLogRepository;
        this.permissionService = permissionService;
    }

    // --- Book Acquisition Requests ---

    @Transactional
    public BookRequestResponse submitBookRequest(Long userId, BookRequestCreateDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        permissionService.validateRequestPermission(user);

        BookRequest request = new BookRequest(user, requestDto.getTitle(), requestDto.getAuthor(), requestDto.getDescription());
        BookRequest saved = bookRequestRepository.save(request);

        activityLogRepository.save(new ActivityLog(user, "BOOK_REQUEST_SUBMITTED", "BOOK_REQUEST", saved.getId(),
                "{\"title\": \"" + requestDto.getTitle() + "\"}", null));

        return mapToBookRequestResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookRequestResponse> getUserBookRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookRequest> requestsPage = bookRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<BookRequestResponse> content = requestsPage.getContent().stream()
                .map(this::mapToBookRequestResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(requestsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookRequestResponse> getAllBookRequests(RequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookRequest> requestsPage = (status != null)
                ? bookRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : bookRequestRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<BookRequestResponse> content = requestsPage.getContent().stream()
                .map(this::mapToBookRequestResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(requestsPage, content);
    }

    @Transactional
    public BookRequestResponse reviewBookRequest(Long requestId, Long adminUserId, RequestReviewDto reviewDto) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BookRequest", "id", requestId));
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        request.setStatus(Boolean.TRUE.equals(reviewDto.getApproved()) ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        request.setAdminResponse(reviewDto.getAdminResponse());
        request.setReviewedBy(adminUser);
        request.setReviewedAt(OffsetDateTime.now());

        BookRequest saved = bookRequestRepository.save(request);
        activityLogRepository.save(new ActivityLog(adminUser, "BOOK_REQUEST_REVIEWED", "BOOK_REQUEST", saved.getId(),
                "{\"status\": \"" + saved.getStatus() + "\"}", null));

        return mapToBookRequestResponse(saved);
    }

    // --- Access Elevation Requests ---

    @Transactional
    public AccessRequestResponse submitAccessRequest(Long userId, AccessRequestCreateDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", requestDto.getBookId()));

        permissionService.validateRequestPermission(user);

        AccessRequest request = new AccessRequest(user, book, requestDto.getReason());
        AccessRequest saved = accessRequestRepository.save(request);

        activityLogRepository.save(new ActivityLog(user, "ACCESS_REQUEST_SUBMITTED", "ACCESS_REQUEST", saved.getId(),
                "{\"book_id\": " + book.getId() + "}", null));

        return mapToAccessRequestResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccessRequestResponse> getUserAccessRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccessRequest> requestsPage = accessRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<AccessRequestResponse> content = requestsPage.getContent().stream()
                .map(this::mapToAccessRequestResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(requestsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccessRequestResponse> getAllAccessRequests(AccessStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccessRequest> requestsPage = (status != null)
                ? accessRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : accessRequestRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<AccessRequestResponse> content = requestsPage.getContent().stream()
                .map(this::mapToAccessRequestResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(requestsPage, content);
    }

    @Transactional
    public AccessRequestResponse reviewAccessRequest(Long requestId, Long adminUserId, RequestReviewDto reviewDto) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", requestId));
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        boolean approved = Boolean.TRUE.equals(reviewDto.getApproved());
        request.setStatus(approved ? AccessStatus.APPROVED : AccessStatus.DECLINED);
        request.setAdminResponse(reviewDto.getAdminResponse());
        request.setReviewedBy(adminUser);
        request.setReviewedAt(OffsetDateTime.now());

        AccessRequest saved = accessRequestRepository.save(request);

        // If approved, automatically create or update the corresponding book_permission grant
        if (approved) {
            int durationDays = reviewDto.getGrantDurationDays() != null && reviewDto.getGrantDurationDays() > 0
                    ? reviewDto.getGrantDurationDays() : 30;

            OffsetDateTime startsAt = OffsetDateTime.now();
            OffsetDateTime expiresAt = startsAt.plusDays(durationDays);

            BookPermission permission = bookPermissionRepository.findByUserIdAndBookId(request.getUser().getId(), request.getBook().getId())
                    .orElseGet(() -> new BookPermission(request.getUser(), request.getBook(), true, reviewDto.getGrantDownload(), startsAt, expiresAt, adminUser));

            permission.setCanRead(true);
            permission.setCanDownload(Boolean.TRUE.equals(reviewDto.getGrantDownload()));
            permission.setStartsAt(startsAt);
            permission.setExpiresAt(expiresAt);
            permission.setCreatedBy(adminUser);

            bookPermissionRepository.save(permission);
        }

        activityLogRepository.save(new ActivityLog(adminUser, "ACCESS_REQUEST_REVIEWED", "ACCESS_REQUEST", saved.getId(),
                "{\"status\": \"" + saved.getStatus() + "\", \"approved\": " + approved + "}", null));

        return mapToAccessRequestResponse(saved);
    }

    private BookRequestResponse mapToBookRequestResponse(BookRequest req) {
        BookRequestResponse dto = new BookRequestResponse();
        dto.setId(req.getId());
        dto.setUserId(req.getUser().getId());
        dto.setUserName(req.getUser().getName());
        dto.setUserEmail(req.getUser().getEmail());
        dto.setTitle(req.getTitle());
        dto.setAuthor(req.getAuthor());
        dto.setDescription(req.getDescription());
        dto.setStatus(req.getStatus());
        dto.setAdminResponse(req.getAdminResponse());
        if (req.getReviewedBy() != null) {
            dto.setReviewedByName(req.getReviewedBy().getName());
        }
        dto.setReviewedAt(req.getReviewedAt());
        dto.setCreatedAt(req.getCreatedAt());
        return dto;
    }

    private AccessRequestResponse mapToAccessRequestResponse(AccessRequest req) {
        AccessRequestResponse dto = new AccessRequestResponse();
        dto.setId(req.getId());
        dto.setUserId(req.getUser().getId());
        dto.setUserName(req.getUser().getName());
        dto.setUserEmail(req.getUser().getEmail());
        dto.setBookId(req.getBook().getId());
        dto.setBookTitle(req.getBook().getTitle());
        dto.setBookAuthor(req.getBook().getAuthor());
        dto.setReason(req.getReason());
        dto.setStatus(req.getStatus());
        dto.setAdminResponse(req.getAdminResponse());
        if (req.getReviewedBy() != null) {
            dto.setReviewedByName(req.getReviewedBy().getName());
        }
        dto.setReviewedAt(req.getReviewedAt());
        dto.setCreatedAt(req.getCreatedAt());
        return dto;
    }
}

