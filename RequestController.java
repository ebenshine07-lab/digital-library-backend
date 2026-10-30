package com.library.controller;

import com.library.dto.common.ApiResponse;
import com.library.dto.common.PagedResponse;
import com.library.dto.request.AccessRequestCreateDto;
import com.library.dto.request.AccessRequestResponse;
import com.library.dto.request.BookRequestCreateDto;
import com.library.dto.request.BookRequestResponse;
import com.library.security.UserPrincipal;
import com.library.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/book-requests")
    public ResponseEntity<ApiResponse<BookRequestResponse>> submitBookRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookRequestCreateDto request) {
        BookRequestResponse response = requestService.submitBookRequest(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book request submitted successfully to library staff.", response));
    }

    @GetMapping("/book-requests")
    public ResponseEntity<ApiResponse<PagedResponse<BookRequestResponse>>> getUserBookRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<BookRequestResponse> response = requestService.getUserBookRequests(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/access-requests")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> submitAccessRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccessRequestCreateDto request) {
        AccessRequestResponse response = requestService.submitAccessRequest(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Access elevation request submitted to library administrators.", response));
    }

    @GetMapping("/access-requests")
    public ResponseEntity<ApiResponse<PagedResponse<AccessRequestResponse>>> getUserAccessRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<AccessRequestResponse> response = requestService.getUserAccessRequests(userPrincipal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

