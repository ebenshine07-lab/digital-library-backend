package com.library.controller;

import com.library.dto.book.BookAccessUrlResponse;
import com.library.dto.book.BookDetailResponse;
import com.library.dto.book.BookSuggestionResponse;
import com.library.dto.book.BookSummaryResponse;
import com.library.dto.category.CategoryResponse;
import com.library.dto.common.ApiResponse;
import com.library.dto.common.PagedResponse;
import com.library.security.UserPrincipal;
import com.library.service.BookService;
import com.library.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookSummaryResponse>>> getBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        PagedResponse<BookSummaryResponse> response = bookService.getBooks(categoryId, typeId, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        BookDetailResponse response = bookService.getBookById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BookSummaryResponse>>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PagedResponse<BookSummaryResponse> response = bookService.searchBooks(query, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<BookSuggestionResponse>> getSuggestions(@RequestParam String query) {
        BookSuggestionResponse response = bookService.getSuggestions(query);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<BookAccessUrlResponse>> requestReadAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        BookAccessUrlResponse response = bookService.requestReadAccess(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Read authorization granted. Temporary stream URL generated.", response));
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<ApiResponse<BookAccessUrlResponse>> requestDownloadAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        BookAccessUrlResponse response = bookService.requestDownloadAccess(id, userPrincipal.getId(), ipAddress);
        return ResponseEntity.ok(ApiResponse.ok("Download authorization granted. Secure download link generated.", response));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }
}

