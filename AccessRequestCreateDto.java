package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccessRequestCreateDto {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotBlank(message = "Reason for access request is required")
    @Size(min = 10, max = 1000, message = "Reason must be between 10 and 1000 characters")
    private String reason;

    public AccessRequestCreateDto() {}

    public AccessRequestCreateDto(Long bookId, String reason) {
        this.bookId = bookId;
        this.reason = reason;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

