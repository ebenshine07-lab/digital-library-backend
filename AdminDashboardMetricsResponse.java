package com.library.dto.dashboard;

public class AdminDashboardMetricsResponse {

    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long totalBooks;
    private long availableBooks;
    private long restrictedBooks;
    private long totalDownloads;
    private long pendingBookRequests;
    private long pendingAccessRequests;
    private long activeRestrictions;

    public AdminDashboardMetricsResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getSuspendedUsers() { return suspendedUsers; }
    public void setSuspendedUsers(long suspendedUsers) { this.suspendedUsers = suspendedUsers; }

    public long getTotalBooks() { return totalBooks; }
    public void setTotalBooks(long totalBooks) { this.totalBooks = totalBooks; }

    public long getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(long availableBooks) { this.availableBooks = availableBooks; }

    public long getRestrictedBooks() { return restrictedBooks; }
    public void setRestrictedBooks(long restrictedBooks) { this.restrictedBooks = restrictedBooks; }

    public long getTotalDownloads() { return totalDownloads; }
    public void setTotalDownloads(long totalDownloads) { this.totalDownloads = totalDownloads; }

    public long getPendingBookRequests() { return pendingBookRequests; }
    public void setPendingBookRequests(long pendingBookRequests) { this.pendingBookRequests = pendingBookRequests; }

    public long getPendingAccessRequests() { return pendingAccessRequests; }
    public void setPendingAccessRequests(long pendingAccessRequests) { this.pendingAccessRequests = pendingAccessRequests; }

    public long getActiveRestrictions() { return activeRestrictions; }
    public void setActiveRestrictions(long activeRestrictions) { this.activeRestrictions = activeRestrictions; }
}

