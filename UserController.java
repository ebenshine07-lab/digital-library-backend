package com.library.controller;

import com.library.dto.common.ApiResponse;
import com.library.dto.common.PagedResponse;
import com.library.dto.dashboard.UserDashboardResponse;
import com.library.dto.history.DownloadHistoryResponse;
import com.library.dto.history.ReadingHistoryResponse;
import com.library.dto.history.ReadingProgressUpdateRequest;
import com.library.dto.user.UserProfileResponse;
import com.library.dto.user.UserUpdateRequest;
import com.library.security.UserPrincipal;
import com.library.service.DashboardService;
import com.library.service.HistoryService;
import com.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class UserController {

    private final UserService userService;
    private final HistoryService historyService;
    private final DashboardService dashboardService;

    public UserController(UserService userService,
                          HistoryService historyService,
                          DashboardService dashboardService) {
        this.userService = userService;
        this.historyService = historyService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserProfileResponse profile = userService.getCurrentUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserUpdateRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", profile));
    }

    /**
     * Aggregated User Home Dashboard endpoint (direct match to UI mockup).
     * Returns:
     * - previousReading carousel
     * - subjects categories with book counters & icons
     * - newBooks row
     * - popularBooks grid
     * - writersAndAuthors list
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<UserDashboardResponse>> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        UserDashboardResponse dashboard = dashboardService.getUserDashboard(userId);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<ReadingHistoryResponse>>> getReadingHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReadingHistoryResponse> history = historyService.getUserReadingHistory(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PutMapping("/history/{bookId}/progress")
    public ResponseEntity<ApiResponse<ReadingHistoryResponse>> updateProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingProgressUpdateRequest request) {
        ReadingHistoryResponse updated = historyService.updateReadingProgress(userPrincipal.getId(), bookId, request);
        return ResponseEntity.ok(ApiResponse.ok("Reading progress updated.", updated));
    }

    @GetMapping("/downloads")
    public ResponseEntity<ApiResponse<PagedResponse<DownloadHistoryResponse>>> getDownloads(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<DownloadHistoryResponse> downloads = historyService.getUserDownloads(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(downloads));
    }
}

