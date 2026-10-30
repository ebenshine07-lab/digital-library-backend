package com.library.dto.permission;

public class UserPermissionDto {

    private Long id;
    private Long userId;
    private boolean canUseApp;
    private boolean canReadBooks;
    private boolean canDownloadBooks;
    private boolean canRequestBooks;

    public UserPermissionDto() {}

    public UserPermissionDto(Long id, Long userId, boolean canUseApp, boolean canReadBooks, boolean canDownloadBooks, boolean canRequestBooks) {
        this.id = id;
        this.userId = userId;
        this.canUseApp = canUseApp;
        this.canReadBooks = canReadBooks;
        this.canDownloadBooks = canDownloadBooks;
        this.canRequestBooks = canRequestBooks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isCanUseApp() { return canUseApp; }
    public void setCanUseApp(boolean canUseApp) { this.canUseApp = canUseApp; }

    public boolean isCanReadBooks() { return canReadBooks; }
    public void setCanReadBooks(boolean canReadBooks) { this.canReadBooks = canReadBooks; }

    public boolean isCanDownloadBooks() { return canDownloadBooks; }
    public void setCanDownloadBooks(boolean canDownloadBooks) { this.canDownloadBooks = canDownloadBooks; }

    public boolean isCanRequestBooks() { return canRequestBooks; }
    public void setCanRequestBooks(boolean canRequestBooks) { this.canRequestBooks = canRequestBooks; }
}

