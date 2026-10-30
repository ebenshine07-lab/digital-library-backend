package com.library.dto.permission;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class BookPermissionCreateDto {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private Boolean canRead = true;
    private Boolean canDownload = false;
    private OffsetDateTime startsAt;
    private OffsetDateTime expiresAt;

    public BookPermissionCreateDto() {}

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Boolean getCanRead() { return canRead; }
    public void setCanRead(Boolean canRead) { this.canRead = canRead; }

    public Boolean getCanDownload() { return canDownload; }
    public void setCanDownload(Boolean canDownload) { this.canDownload = canDownload; }

    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}

