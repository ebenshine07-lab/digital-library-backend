package com.library.dto.history;

import java.time.OffsetDateTime;

public class DownloadHistoryResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverPath;
    private OffsetDateTime downloadedAt;

    public DownloadHistoryResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public OffsetDateTime getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(OffsetDateTime downloadedAt) { this.downloadedAt = downloadedAt; }
}

