package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RequestReviewDto {

    @NotNull(message = "Review status decision is required")
    private Boolean approved;

    @NotBlank(message = "Administrator response is required")
    private String adminResponse;

    // Optional duration (in days) if approving an access request to create a book_permission
    private Integer grantDurationDays = 30;

    private Boolean grantDownload = false;

    public RequestReviewDto() {}

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public Integer getGrantDurationDays() { return grantDurationDays; }
    public void setGrantDurationDays(Integer grantDurationDays) { this.grantDurationDays = grantDurationDays; }

    public Boolean getGrantDownload() { return grantDownload; }
    public void setGrantDownload(Boolean grantDownload) { this.grantDownload = grantDownload; }
}

