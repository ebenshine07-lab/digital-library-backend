package com.library.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_permissions")
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "can_use_app", nullable = false)
    private Boolean canUseApp = true;

    @Column(name = "can_read_books", nullable = false)
    private Boolean canReadBooks = true;

    @Column(name = "can_download_books", nullable = false)
    private Boolean canDownloadBooks = true;

    @Column(name = "can_request_books", nullable = false)
    private Boolean canRequestBooks = true;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UserPermission() {}

    public UserPermission(User user) {
        this.user = user;
        this.canUseApp = true;
        this.canReadBooks = true;
        this.canDownloadBooks = true;
        this.canRequestBooks = true;
        this.updatedAt = OffsetDateTime.now();
    }

    public UserPermission(User user, boolean canUseApp, boolean canReadBooks, boolean canDownloadBooks, boolean canRequestBooks) {
        this.user = user;
        this.canUseApp = canUseApp;
        this.canReadBooks = canReadBooks;
        this.canDownloadBooks = canDownloadBooks;
        this.canRequestBooks = canRequestBooks;
        this.updatedAt = OffsetDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (canUseApp == null) canUseApp = true;
        if (canReadBooks == null) canReadBooks = true;
        if (canDownloadBooks == null) canDownloadBooks = true;
        if (canRequestBooks == null) canRequestBooks = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Boolean getCanUseApp() { return canUseApp; }
    public void setCanUseApp(Boolean canUseApp) { this.canUseApp = canUseApp; }

    public Boolean getCanReadBooks() { return canReadBooks; }
    public void setCanReadBooks(Boolean canReadBooks) { this.canReadBooks = canReadBooks; }

    public Boolean getCanDownloadBooks() { return canDownloadBooks; }
    public void setCanDownloadBooks(Boolean canDownloadBooks) { this.canDownloadBooks = canDownloadBooks; }

    public Boolean getCanRequestBooks() { return canRequestBooks; }
    public void setCanRequestBooks(Boolean canRequestBooks) { this.canRequestBooks = canRequestBooks; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

