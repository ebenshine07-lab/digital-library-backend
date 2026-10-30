package com.library.dto.user;

import com.library.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {

    @NotNull(message = "User status is required")
    private UserStatus status;

    private String reason;

    public UserStatusUpdateRequest() {}

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

