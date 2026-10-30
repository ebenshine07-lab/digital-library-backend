package com.library.service;

import com.library.entity.*;
import com.library.exception.AccessDeniedException;
import com.library.exception.AccountRestrictedException;
import com.library.exception.AccountSuspendedException;
import com.library.repository.BookPermissionRepository;
import com.library.repository.UserPermissionRepository;
import com.library.repository.UserRestrictionRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @Mock
    private UserRestrictionRepository userRestrictionRepository;

    @Mock
    private BookPermissionRepository bookPermissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private User activeUser;
    private User suspendedUser;
    private Book availableBook;
    private Book restrictedBook;
    private UserPermission defaultPerms;

    @BeforeEach
    void setUp() {
        activeUser = new User("Abhishek Saha", "abhishek@library.com", "123456", "hashed_pass", Role.ROLE_USER);
        activeUser.setId(2L);
        activeUser.setStatus(UserStatus.ACTIVE);

        suspendedUser = new User("Suspended User", "suspended@library.com", "123456", "hashed_pass", Role.ROLE_USER);
        suspendedUser.setId(5L);
        suspendedUser.setStatus(UserStatus.SUSPENDED);

        Category category = new Category("Design", "Design Books", "pen-tool");
        category.setId(1L);

        BookType bookType = new BookType("E-Book");
        bookType.setId(1L);

        availableBook = new Book("Steal Like an Artist", "Austin Kleon", "Creativity", category, bookType,
                "pdfs/steal.pdf", "covers/steal.jpg", BookStatus.AVAILABLE, 160, 4000000L, activeUser);
        availableBook.setId(10L);

        restrictedBook = new Book("Murder on the Orient Express", "Agatha Christie", "Mystery", category, bookType,
                "pdfs/orient.pdf", "covers/orient.jpg", BookStatus.RESTRICTED, 250, 5000000L, activeUser);
        restrictedBook.setId(20L);

        defaultPerms = new UserPermission(activeUser, true, true, true, true);
    }

    @Test
    @DisplayName("Active user with default permissions can read available book")
    void shouldAllowReadForActiveUserOnAvailableBook() {
        when(userPermissionRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(defaultPerms));
        when(userRestrictionRepository.findActiveRestrictions(eq(activeUser.getId()), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> permissionService.validateReadPermission(activeUser, availableBook));
    }

    @Test
    @DisplayName("Suspended user is immediately blocked from reading")
    void shouldThrowWhenUserIsSuspended() {
        assertThrows(AccountSuspendedException.class, () ->
                permissionService.validateReadPermission(suspendedUser, availableBook));
    }

    @Test
    @DisplayName("Active BLOCK_READING restriction blocks reading even if book is available")
    void shouldBlockReadingWhenActiveRestrictionExists() {
        UserRestriction restriction = new UserRestriction(activeUser, RestrictionType.BLOCK_READING,
                "Misconduct", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(5), activeUser);

        when(userRestrictionRepository.findActiveRestrictions(eq(activeUser.getId()), any(OffsetDateTime.class)))
                .thenReturn(List.of(restriction));
        when(userPermissionRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(defaultPerms));

        assertThrows(AccountRestrictedException.class, () ->
                permissionService.validateReadPermission(activeUser, availableBook));
    }

    @Test
    @DisplayName("Restricted book is blocked if user has no BookPermission override")
    void shouldBlockRestrictedBookWithoutGrant() {
        when(userPermissionRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(defaultPerms));
        when(userRestrictionRepository.findActiveRestrictions(eq(activeUser.getId()), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookPermissionRepository.findActivePermission(eq(activeUser.getId()), eq(restrictedBook.getId()), any(OffsetDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                permissionService.validateReadPermission(activeUser, restrictedBook));
    }

    @Test
    @DisplayName("Restricted book is allowed when active BookPermission grant exists")
    void shouldAllowRestrictedBookWithValidGrant() {
        BookPermission grant = new BookPermission(activeUser, restrictedBook, true, false,
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(30), activeUser);

        when(userPermissionRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(defaultPerms));
        when(userRestrictionRepository.findActiveRestrictions(eq(activeUser.getId()), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookPermissionRepository.findActivePermission(eq(activeUser.getId()), eq(restrictedBook.getId()), any(OffsetDateTime.class)))
                .thenReturn(Optional.of(grant));

        assertDoesNotThrow(() -> permissionService.validateReadPermission(activeUser, restrictedBook));
    }

    @Test
    @DisplayName("Download is blocked when BLOCK_DOWNLOADING restriction is active")
    void shouldBlockDownloadWhenRestrictionActive() {
        UserRestriction restriction = new UserRestriction(activeUser, RestrictionType.BLOCK_DOWNLOADING,
                "Excessive bandwidth", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(7), activeUser);

        when(userRestrictionRepository.findActiveRestrictions(eq(activeUser.getId()), any(OffsetDateTime.class)))
                .thenReturn(List.of(restriction));
        when(userPermissionRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(defaultPerms));

        assertThrows(AccountRestrictedException.class, () ->
                permissionService.validateDownloadPermission(activeUser, availableBook));
    }
}

