package com.library.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "downloads")
public class Download {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "downloaded_at", nullable = false, updatable = false)
    private OffsetDateTime downloadedAt = OffsetDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public Download() {}

    public Download(User user, Book book, String ipAddress) {
        this.user = user;
        this.book = book;
        this.ipAddress = ipAddress;
        this.downloadedAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (downloadedAt == null) downloadedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public OffsetDateTime getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(OffsetDateTime downloadedAt) { this.downloadedAt = downloadedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}

