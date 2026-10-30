package com.library.service;

import com.library.entity.*;
import com.library.exception.AccessDeniedException;
import com.library.exception.AccountRestrictedException;
import com.library.exception.AccountSuspendedException;
import com.library.exception.BadRequestException;
import com.library.repository.BookPermissionRepository;
import com.library.repository.UserPermissionRepository;
import com.library.repository.UserRestrictionRepository;
import com.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserRestrictionRepository userRestrictionRepository;
    private final BookPermissionRepository bookPermissionRepository;

    public PermissionService(UserRepository userRepository,
                             UserPermissionRepository userPermissionRepository,
                             UserRestrictionRepository userRestrictionRepository,
                             BookPermissionRepository bookPermissionRepository) {
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.userRestrictionRepository = userRestrictionRepository;
        this.bookPermissionRepository = bookPermissionRepository;
    }

    /**
     * Centralized Permission Validation Engine
     * Enforces strict priority hierarchy:
     * 1. User status check (ACTIVE vs SUSPENDED/DEACTIVATED)
     * 2. General app permission check (can_use_app)
     * 3. Active timed user restrictions (BLOCK_READING, BLOCK_DOWNLOADING, SUSPEND_ALL)
     * 4. General action permission (can_read_books, can_download_books, can_request_books)
     * 5. Book availability status (AVAILABLE vs RESTRICTED) and specific book permissions
     */
    @Transactional(readOnly = true)
    public void validateReadPermission(User user, Book book) {
        // Step 1: User Account Status
        validateUserActive(user);

        // Step 2 & 3: User App & Active Restriction Check
        OffsetDateTime now = OffsetDateTime.now();
        List<UserRestriction> activeRestrictions = userRestrictionRepository.findActiveRestrictions(user.getId(), now);
        for (UserRestriction restriction : activeRestrictions) {
            if (restriction.getRestrictionType() == RestrictionType.SUSPEND_ALL) {
                throw new AccountRestrictedException("Account access is temporarily suspended: " + restriction.getReason());
            }
            if (restriction.getRestrictionType() == RestrictionType.BLOCK_READING) {
                throw new AccountRestrictedException("Your reading privilege is temporarily restricted: " + restriction.getReason());
            }
        }

        // Step 4: General Reading Permission
        UserPermission perms = getOrCreateUserPermission(user);
        if (!perms.getCanReadBooks()) {
            throw new AccessDeniedException("Your account does not possess permission to read books.");
        }

        // Step 5: Book Status & Book Specific Permissions
        if (book.getStatus() == BookStatus.ARCHIVED) {
            throw new BadRequestException("This book has been archived and is no longer accessible.");
        }

        if (book.getStatus() == BookStatus.RESTRICTED) {
            Optional<BookPermission> bookPermOpt = bookPermissionRepository.findActivePermission(user.getId(), book.getId(), now);
            if (bookPermOpt.isEmpty() || !bookPermOpt.get().getCanRead()) {
                throw new AccessDeniedException("This document is restricted. You must submit an access request to the librarian to read it.");
            }
        }
    }

    @Transactional(readOnly = true)
    public void validateDownloadPermission(User user, Book book) {
        // Step 1: User Account Status
        validateUserActive(user);

        // Step 2 & 3: User App & Active Restriction Check
        OffsetDateTime now = OffsetDateTime.now();
        List<UserRestriction> activeRestrictions = userRestrictionRepository.findActiveRestrictions(user.getId(), now);
        for (UserRestriction restriction : activeRestrictions) {
            if (restriction.getRestrictionType() == RestrictionType.SUSPEND_ALL) {
                throw new AccountRestrictedException("Account access is temporarily suspended: " + restriction.getReason());
            }
            if (restriction.getRestrictionType() == RestrictionType.BLOCK_DOWNLOADING) {
                throw new AccountRestrictedException("Your downloading privilege is temporarily restricted: " + restriction.getReason());
            }
        }

        // Step 4: General Download Permission
        UserPermission perms = getOrCreateUserPermission(user);
        if (!perms.getCanDownloadBooks()) {
            throw new AccessDeniedException("Your account is not permitted to download digital books.");
        }

        // Step 5: Book Status & Book-Specific Overrides
        if (book.getStatus() == BookStatus.ARCHIVED) {
            throw new BadRequestException("This book has been archived and is not available for download.");
        }

        if (book.getStatus() == BookStatus.RESTRICTED) {
            Optional<BookPermission> bookPermOpt = bookPermissionRepository.findActivePermission(user.getId(), book.getId(), now);
            if (bookPermOpt.isEmpty() || !bookPermOpt.get().getCanDownload()) {
                throw new AccessDeniedException("This document is restricted. You must submit an access request to obtain download authorization.");
            }
        }
    }

    @Transactional(readOnly = true)
    public void validateRequestPermission(User user) {
        validateUserActive(user);

        OffsetDateTime now = OffsetDateTime.now();
        List<UserRestriction> activeRestrictions = userRestrictionRepository.findActiveRestrictions(user.getId(), now);
        for (UserRestriction restriction : activeRestrictions) {
            if (restriction.getRestrictionType() == RestrictionType.SUSPEND_ALL ||
                restriction.getRestrictionType() == RestrictionType.BLOCK_REQUESTING) {
                throw new AccountRestrictedException("Your privilege to submit requests is temporarily restricted: " + restriction.getReason());
            }
        }

        UserPermission perms = getOrCreateUserPermission(user);
        if (!perms.getCanRequestBooks()) {
            throw new AccessDeniedException("Your account does not have permission to submit new requests.");
        }
    }

    @Transactional(readOnly = true)
    public boolean canUserRead(User user, Book book) {
        try {
            validateReadPermission(user, book);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean canUserDownload(User user, Book book) {
        try {
            validateDownloadPermission(user, book);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void validateUserActive(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountSuspendedException("Your library account has been suspended by an administrator. Please contact library services.");
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new AccessDeniedException("Your library account is deactivated.");
        }

        UserPermission perms = getOrCreateUserPermission(user);
        if (!perms.getCanUseApp()) {
            throw new AccessDeniedException("Your account access to the Digital Library system has been revoked.");
        }
    }

    public UserPermission getOrCreateUserPermission(User user) {
        return userPermissionRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPermission newPerm = new UserPermission(user);
                    return userPermissionRepository.save(newPerm);
                });
    }
}

