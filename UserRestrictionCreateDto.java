package com.library.dto.permission;

import com.library.entity.RestrictionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class UserRestrictionCreateDto {

    @NotNull(message = "Restriction type is required")
    private RestrictionType restrictionType;

    @NotBlank(message = "Reason is required")
    private String reason;

    private OffsetDateTime startsAt;

    @NotNull(message = "Expiration time is required")
    @Future(message = "Expiration time must be in the future")
    private OffsetDateTime expiresAt;

    public UserRestrictionCreateDto() {}

    public RestrictionType getRestrictionType() { return restrictionType; }
    public void setRestrictionType(RestrictionType restrictionType) { this.restrictionType = restrictionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}

