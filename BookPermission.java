package com.library.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "book_permissions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_book_perms_user_book", columnNames = {"user_id", "book_id"})
})
public class BookPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "can_read", nullable = false)
    private Boolean canRead = true;

    @Column(name = "can_download", nullable = false)
    private Boolean canDownload = false;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt = OffsetDateTime.now();

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public BookPermission() {}

    public BookPermission(User user, Book book, Boolean canRead, Boolean canDownload,
                          OffsetDateTime startsAt, OffsetDateTime expiresAt, User createdBy) {
        this.user = user;
        this.book = book;
        this.canRead = canRead != null ? canRead : true;
        this.canDownload = canDownload != null ? canDownload : false;
        this.startsAt = startsAt != null ? startsAt : OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (startsAt == null) startsAt = OffsetDateTime.now();
        if (canRead == null) canRead = true;
        if (canDownload == null) canDownload = false;
    }

    public boolean isCurrentlyEffective() {
        OffsetDateTime now = OffsetDateTime.now();
        return (startsAt == null || !now.isBefore(startsAt)) && (expiresAt == null || now.isBefore(expiresAt));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Boolean getCanRead() { return canRead; }
    public void setCanRead(Boolean canRead) { this.canRead = canRead; }

    public Boolean getCanDownload() { return canDownload; }
    public void setCanDownload(Boolean canDownload) { this.canDownload = canDownload; }

    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

