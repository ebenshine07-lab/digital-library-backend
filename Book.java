package com.library.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private BookType type;

    @Column(name = "pdf_path", nullable = false, length = 500)
    private String pdfPath;

    @Column(name = "cover_path", nullable = false, length = 500)
    private String coverPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(name = "total_pages", nullable = false)
    private Integer totalPages = 1;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes = 0L;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingHistory> readingHistories = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Download> downloads = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookPermission> bookPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccessRequest> accessRequests = new ArrayList<>();

    public Book() {}

    public Book(String title, String author, String description, Category category, BookType type,
                String pdfPath, String coverPath, BookStatus status, Integer totalPages,
                Long fileSizeBytes, User createdBy) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.type = type;
        this.pdfPath = pdfPath;
        this.coverPath = coverPath;
        this.status = status != null ? status : BookStatus.AVAILABLE;
        this.totalPages = totalPages != null && totalPages > 0 ? totalPages : 1;
        this.fileSizeBytes = fileSizeBytes != null ? fileSizeBytes : 0L;
        this.createdBy = createdBy;
        this.downloadCount = 0;
        this.viewCount = 0;
        this.isFeatured = false;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (status == null) status = BookStatus.AVAILABLE;
        if (totalPages == null || totalPages < 1) totalPages = 1;
        if (downloadCount == null) downloadCount = 0;
        if (viewCount == null) viewCount = 0;
        if (fileSizeBytes == null) fileSizeBytes = 0L;
        if (isFeatured == null) isFeatured = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public BookType getType() { return type; }
    public void setType(BookType type) { this.type = type; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ReadingHistory> getReadingHistories() { return readingHistories; }
    public void setReadingHistories(List<ReadingHistory> readingHistories) { this.readingHistories = readingHistories; }

    public List<Download> getDownloads() { return downloads; }
    public void setDownloads(List<Download> downloads) { this.downloads = downloads; }

    public List<BookPermission> getBookPermissions() { return bookPermissions; }
    public void setBookPermissions(List<BookPermission> bookPermissions) { this.bookPermissions = bookPermissions; }

    public List<AccessRequest> getAccessRequests() { return accessRequests; }
    public void setAccessRequests(List<AccessRequest> accessRequests) { this.accessRequests = accessRequests; }
}

