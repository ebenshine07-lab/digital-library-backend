package com.library.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reading_history", uniqueConstraints = {
    @UniqueConstraint(name = "uq_history_user_book", columnNames = {"user_id", "book_id"})
})
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "current_page", nullable = false)
    private Integer currentPage = 1;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal progress = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "last_read_at", nullable = false)
    private OffsetDateTime lastReadAt = OffsetDateTime.now();

    public ReadingHistory() {}

    public ReadingHistory(User user, Book book, Integer currentPage, BigDecimal progress) {
        this.user = user;
        this.book = book;
        this.currentPage = currentPage != null && currentPage > 0 ? currentPage : 1;
        this.progress = progress != null ? progress : BigDecimal.ZERO;
        this.completed = this.progress.compareTo(BigDecimal.valueOf(100)) >= 0;
        this.startedAt = OffsetDateTime.now();
        this.lastReadAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = OffsetDateTime.now();
        if (lastReadAt == null) lastReadAt = OffsetDateTime.now();
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (progress == null) progress = BigDecimal.ZERO;
        if (completed == null) completed = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) {
        this.progress = progress;
        if (progress != null && progress.compareTo(BigDecimal.valueOf(100.00)) >= 0) {
            this.completed = true;
        }
    }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(OffsetDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}

