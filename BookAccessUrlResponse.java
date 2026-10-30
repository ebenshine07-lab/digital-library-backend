package com.library.dto.book;

public class BookAccessUrlResponse {

    private String accessUrl;
    private long expiresInSeconds;
    private String bookTitle;
    private String action; // READ or DOWNLOAD

    public BookAccessUrlResponse() {}

    public BookAccessUrlResponse(String accessUrl, long expiresInSeconds, String bookTitle, String action) {
        this.accessUrl = accessUrl;
        this.expiresInSeconds = expiresInSeconds;
        this.bookTitle = bookTitle;
        this.action = action;
    }

    public String getAccessUrl() { return accessUrl; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }

    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}

