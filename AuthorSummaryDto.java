package com.library.dto.dashboard;

public class AuthorSummaryDto {

    private String name;
    private long bookCount;
    private String avatarUrl;

    public AuthorSummaryDto() {}

    public AuthorSummaryDto(String name, long bookCount, String avatarUrl) {
        this.name = name;
        this.bookCount = bookCount;
        this.avatarUrl = avatarUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getBookCount() { return bookCount; }
    public void setBookCount(long bookCount) { this.bookCount = bookCount; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

