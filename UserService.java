package com.library.service;

import com.library.dto.common.PagedResponse;
import com.library.dto.permission.*;
import com.library.dto.user.*;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserRestrictionRepository userRestrictionRepository;
    private final BookPermissionRepository bookPermissionRepository;
    private final BookRepository bookRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final DownloadRepository downloadRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserPermissionRepository userPermissionRepository,
                       UserRestrictionRepository userRestrictionRepository,
                       BookPermissionRepository bookPermissionRepository,
                       BookRepository bookRepository,
                       ReadingHistoryRepository readingHistoryRepository,
                       DownloadRepository downloadRepository,
                       ActivityLogRepository activityLogRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.userRestrictionRepository = userRestrictionRepository;
        this.bookPermissionRepository = bookPermissionRepository;
        this.bookRepository = bookRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.downloadRepository = downloadRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserPermission permissions = userPermissionRepository.findByUserId(userId)
                .orElseGet(() -> new UserPermission(user));

        List<UserRestriction> activeRestrictions = userRestrictionRepository.findActiveRestrictions(userId, OffsetDateTime.now());

        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());

        dto.setPermissions(new UserPermissionDto(
                permissions.getId(),
                user.getId(),
                permissions.getCanUseApp(),
                permissions.getCanReadBooks(),
                permissions.getCanDownloadBooks(),
                permissions.getCanRequestBooks()
        ));

        dto.setActiveRestrictions(activeRestrictions.stream()
                .map(this::mapToRestrictionResponse)
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
        return getCurrentUserProfile(userId);
    }

    // --- Administrator User Management ---

    @Transactional(readOnly = true)
    public PagedResponse<UserAdminResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage;
        if (keyword != null && !keyword.isBlank()) {
            usersPage = userRepository.searchUsers(keyword.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        List<UserAdminResponse> content = usersPage.getContent().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(usersPage, content);
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserAdminDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToAdminResponse(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatusUpdateRequest request, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User admin = userRepository.findById(adminId).orElse(null);

        user.setStatus(request.getStatus());
        userRepository.save(user);

        activityLogRepository.save(new ActivityLog(admin, "USER_STATUS_UPDATED", "USER", userId,
                "{\"status\": \"" + request.getStatus() + "\", \"reason\": \"" + request.getReason() + "\"}", null));
    }

    @Transactional
    public UserPermissionDto updateUserPermissions(Long userId, UserPermissionDto dto, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User admin = userRepository.findById(adminId).orElse(null);

        UserPermission perms = userPermissionRepository.findByUserId(userId)
                .orElseGet(() -> new UserPermission(user));

        perms.setCanUseApp(dto.isCanUseApp());
        perms.setCanReadBooks(dto.isCanReadBooks());
        perms.setCanDownloadBooks(dto.isCanDownloadBooks());
        perms.setCanRequestBooks(dto.isCanRequestBooks());

        UserPermission saved = userPermissionRepository.save(perms);

        activityLogRepository.save(new ActivityLog(admin, "USER_PERMISSIONS_UPDATED", "USER", userId,
                "{\"can_read\": " + dto.isCanReadBooks() + ", \"can_download\": " + dto.isCanDownloadBooks() + "}", null));

        return new UserPermissionDto(saved.getId(), userId, saved.getCanUseApp(),
                saved.getCanReadBooks(), saved.getCanDownloadBooks(), saved.getCanRequestBooks());
    }

    @Transactional
    public UserRestrictionResponse applyUserRestriction(Long userId, UserRestrictionCreateDto dto, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        OffsetDateTime startsAt = dto.getStartsAt() != null ? dto.getStartsAt() : OffsetDateTime.now();
        if (dto.getExpiresAt().isBefore(startsAt)) {
            throw new BadRequestException("Restriction expiration time must be after the start time.");
        }

        UserRestriction restriction = new UserRestriction(user, dto.getRestrictionType(), dto.getReason(), startsAt, dto.getExpiresAt(), admin);
        UserRestriction saved = userRestrictionRepository.save(restriction);

        activityLogRepository.save(new ActivityLog(admin, "USER_RESTRICTION_APPLIED", "USER", userId,
                "{\"type\": \"" + dto.getRestrictionType() + "\", \"reason\": \"" + dto.getReason() + "\"}", null));

        return mapToRestrictionResponse(saved);
    }

    @Transactional
    public void removeUserRestriction(Long restrictionId, Long adminId) {
        UserRestriction restriction = userRestrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new ResourceNotFoundException("UserRestriction", "id", restrictionId));
        User admin = userRepository.findById(adminId).orElse(null);

        restriction.setIsActive(false);
        userRestrictionRepository.save(restriction);

        activityLogRepository.save(new ActivityLog(admin, "USER_RESTRICTION_REVOKED", "USER_RESTRICTION", restrictionId, null, null));
    }

    @Transactional
    public BookPermissionResponse grantBookPermission(Long userId, BookPermissionCreateDto dto, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", dto.getBookId()));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        OffsetDateTime startsAt = dto.getStartsAt() != null ? dto.getStartsAt() : OffsetDateTime.now();
        BookPermission permission = bookPermissionRepository.findByUserIdAndBookId(userId, dto.getBookId())
                .orElseGet(() -> new BookPermission(user, book, dto.getCanRead(), dto.getCanDownload(), startsAt, dto.getExpiresAt(), admin));

        permission.setCanRead(Boolean.TRUE.equals(dto.getCanRead()));
        permission.setCanDownload(Boolean.TRUE.equals(dto.getCanDownload()));
        permission.setStartsAt(startsAt);
        permission.setExpiresAt(dto.getExpiresAt());
        permission.setCreatedBy(admin);

        BookPermission saved = bookPermissionRepository.save(permission);
        return mapToBookPermissionResponse(saved);
    }

    private UserAdminResponse mapToAdminResponse(User user) {
        UserAdminResponse dto = new UserAdminResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setCompletedBooksCount(readingHistoryRepository.countCompletedByUserId(user.getId()));
        dto.setDownloadsCount(downloadRepository.countByUserId(user.getId()));

        List<UserRestriction> active = userRestrictionRepository.findActiveRestrictions(user.getId(), OffsetDateTime.now());
        dto.setHasActiveRestrictions(!active.isEmpty());
        return dto;
    }

    private UserRestrictionResponse mapToRestrictionResponse(UserRestriction r) {
        UserRestrictionResponse dto = new UserRestrictionResponse();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setRestrictionType(r.getRestrictionType());
        dto.setReason(r.getReason());
        dto.setStartsAt(r.getStartsAt());
        dto.setExpiresAt(r.getExpiresAt());
        dto.setActive(r.isCurrentlyEffective());
        dto.setCreatedBy(r.getCreatedBy().getId());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private BookPermissionResponse mapToBookPermissionResponse(BookPermission bp) {
        BookPermissionResponse dto = new BookPermissionResponse();
        dto.setId(bp.getId());
        dto.setUserId(bp.getUser().getId());
        dto.setBookId(bp.getBook().getId());
        dto.setBookTitle(bp.getBook().getTitle());
        dto.setCanRead(bp.getCanRead());
        dto.setCanDownload(bp.getCanDownload());
        dto.setStartsAt(bp.getStartsAt());
        dto.setExpiresAt(bp.getExpiresAt());
        dto.setCreatedBy(bp.getCreatedBy().getId());
        dto.setCreatedAt(bp.getCreatedAt());
        return dto;
    }
}

