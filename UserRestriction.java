package com.library.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_restrictions")
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false, length = 50)
    private RestrictionType restrictionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt = OffsetDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UserRestriction() {}

    public UserRestriction(User user, RestrictionType restrictionType, String reason,
                           OffsetDateTime startsAt, OffsetDateTime expiresAt, User createdBy) {
        this.user = user;
        this.restrictionType = restrictionType;
        this.reason = reason;
        this.startsAt = startsAt != null ? startsAt : OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (startsAt == null) startsAt = OffsetDateTime.now();
        if (isActive == null) isActive = true;
    }

    public boolean isCurrentlyEffective() {
        if (Boolean.FALSE.equals(isActive)) return false;
        OffsetDateTime now = OffsetDateTime.now();
        return (startsAt == null || !now.isBefore(startsAt)) && (expiresAt == null || now.isBefore(expiresAt));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RestrictionType getRestrictionType() { return restrictionType; }
    public void setRestrictionType(RestrictionType restrictionType) { this.restrictionType = restrictionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

