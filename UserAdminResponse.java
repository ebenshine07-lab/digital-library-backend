package com.library.dto.user;

import com.library.entity.Role;
import com.library.entity.UserStatus;

import java.time.OffsetDateTime;

public class UserAdminResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private long completedBooksCount;
    private long downloadsCount;
    private boolean hasActiveRestrictions;

    public UserAdminResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public long getCompletedBooksCount() { return completedBooksCount; }
    public void setCompletedBooksCount(long completedBooksCount) { this.completedBooksCount = completedBooksCount; }

    public long getDownloadsCount() { return downloadsCount; }
    public void setDownloadsCount(long downloadsCount) { this.downloadsCount = downloadsCount; }

    public boolean isHasActiveRestrictions() { return hasActiveRestrictions; }
    public void setHasActiveRestrictions(boolean hasActiveRestrictions) { this.hasActiveRestrictions = hasActiveRestrictions; }
}

