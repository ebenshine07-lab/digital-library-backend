package com.library.controller;

import com.library.dto.book.BookCreateRequest;
import com.library.dto.book.BookDetailResponse;
import com.library.dto.book.BookSummaryResponse;
import com.library.dto.book.BookUpdateRequest;
import com.library.dto.category.CategoryRequest;
import com.library.dto.category.CategoryResponse;
import com.library.dto.common.ApiResponse;
import com.library.dto.common.PagedResponse;
import com.library.dto.dashboard.AdminDashboardMetricsResponse;
import com.library.dto.permission.*;
import com.library.dto.request.AccessRequestResponse;
import com.library.dto.request.BookRequestResponse;
import com.library.dto.request.RequestReviewDto;
import com.library.dto.user.UserAdminResponse;
import com.library.dto.user.UserStatusUpdateRequest;
import com.library.entity.AccessStatus;
import com.library.entity.ActivityLog;
import com.library.entity.RequestStatus;
import com.library.repository.ActivityLogRepository;
import com.library.security.UserPrincipal;
import com.library.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final BookService bookService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final StorageService storageService;
    private final ActivityLogRepository activityLogRepository;

    public AdminController(DashboardService dashboardService,
                           BookService bookService,
                           UserService userService,
                           CategoryService categoryService,
                           RequestService requestService,
                           StorageService storageService,
                           ActivityLogRepository activityLogRepository) {
        this.dashboardService = dashboardService;
        this.bookService = bookService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.requestService = requestService;
        this.storageService = storageService;
        this.activityLogRepository = activityLogRepository;
    }

    // --- 1. Dashboard Metrics ---
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardMetricsResponse>> getMetrics() {
        AdminDashboardMetricsResponse metrics = dashboardService.getAdminDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    // --- 2. Book Management ---
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<PagedResponse<BookSummaryResponse>>> getBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        PagedResponse<BookSummaryResponse> response = bookService.getBooks(categoryId, typeId, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/books")
    public ResponseEntity<ApiResponse<BookDetailResponse>> createBook(
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody BookCreateRequest request) {
        BookDetailResponse response = bookService.createBook(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book catalog entry created successfully.", response));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin) {
        BookDetailResponse response = bookService.getBookById(id, admin.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookDetailResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Book catalog entry updated.", response));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book marked as archived.", null));
    }

    @PostMapping("/books/{id}/restrict")
    public ResponseEntity<ApiResponse<Void>> restrictBook(@PathVariable Long id) {
        bookService.restrictBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book restricted successfully.", null));
    }

    @PostMapping("/books/{id}/unrestrict")
    public ResponseEntity<ApiResponse<Void>> unrestrictBook(@PathVariable Long id) {
        bookService.unrestrictBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book unrestricted successfully.", null));
    }

    // --- 3. Storage Uploads ---
    @PostMapping(value = "/upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPdf(@RequestParam("file") MultipartFile file) {
        String path = storageService.uploadPdf(file);
        return ResponseEntity.ok(ApiResponse.ok("PDF file uploaded successfully.", Map.of("storagePath", path)));
    }

    @PostMapping(value = "/upload/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCover(@RequestParam("file") MultipartFile file) {
        String url = storageService.uploadCoverImage(file);
        return ResponseEntity.ok(ApiResponse.ok("Cover image uploaded successfully.", Map.of("coverUrl", url)));
    }

    // --- 4. Category Management ---
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllActiveCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created.", response));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Category updated.", response));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted or deactivated.", null));
    }

    // --- 5. User Governance ---
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserAdminResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagedResponse<UserAdminResponse> response = userService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserDetail(@PathVariable Long id) {
        UserAdminResponse response = userService.getUserAdminDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(id, request, admin.getId());
        return ResponseEntity.ok(ApiResponse.ok("User status updated.", null));
    }

    @PutMapping("/users/{id}/permissions")
    public ResponseEntity<ApiResponse<UserPermissionDto>> updateUserPermissions(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody UserPermissionDto dto) {
        UserPermissionDto response = userService.updateUserPermissions(id, dto, admin.getId());
        return ResponseEntity.ok(ApiResponse.ok("User general permissions updated.", response));
    }

    @PostMapping("/users/{id}/restrictions")
    public ResponseEntity<ApiResponse<UserRestrictionResponse>> applyUserRestriction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody UserRestrictionCreateDto dto) {
        UserRestrictionResponse response = userService.applyUserRestriction(id, dto, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Timed restriction applied to user.", response));
    }

    @DeleteMapping("/restrictions/{restrictionId}")
    public ResponseEntity<ApiResponse<Void>> revokeUserRestriction(
            @PathVariable Long restrictionId,
            @AuthenticationPrincipal UserPrincipal admin) {
        userService.removeUserRestriction(restrictionId, admin.getId());
        return ResponseEntity.ok(ApiResponse.ok("User restriction revoked.", null));
    }

    @PostMapping("/users/{id}/book-permissions")
    public ResponseEntity<ApiResponse<BookPermissionResponse>> grantBookPermission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody BookPermissionCreateDto dto) {
        BookPermissionResponse response = userService.grantBookPermission(id, dto, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book-specific permission granted.", response));
    }

    // --- 6. Request Reviews ---
    @GetMapping("/book-requests")
    public ResponseEntity<ApiResponse<PagedResponse<BookRequestResponse>>> getBookRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagedResponse<BookRequestResponse> response = requestService.getAllBookRequests(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/book-requests/{id}")
    public ResponseEntity<ApiResponse<BookRequestResponse>> reviewBookRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody RequestReviewDto reviewDto) {
        BookRequestResponse response = requestService.reviewBookRequest(id, admin.getId(), reviewDto);
        return ResponseEntity.ok(ApiResponse.ok("Book request reviewed.", response));
    }

    @GetMapping("/access-requests")
    public ResponseEntity<ApiResponse<PagedResponse<AccessRequestResponse>>> getAccessRequests(
            @RequestParam(required = false) AccessStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagedResponse<AccessRequestResponse> response = requestService.getAllAccessRequests(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/access-requests/{id}")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> reviewAccessRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody RequestReviewDto reviewDto) {
        AccessRequestResponse response = requestService.reviewAccessRequest(id, admin.getId(), reviewDto);
        return ResponseEntity.ok(ApiResponse.ok("Access request processed. Permissions updated.", response));
    }

    // --- 7. Activity Logs ---
    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityLog>>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ActivityLog> logs = activityLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        PagedResponse<ActivityLog> response = PagedResponse.of(logs, logs.getContent());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

