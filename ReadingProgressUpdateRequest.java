package com.library.dto.history;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReadingProgressUpdateRequest {

    @NotNull(message = "Current page is required")
    @Min(value = 1, message = "Current page must be at least 1")
    private Integer currentPage;

    @DecimalMin(value = "0.00", message = "Progress cannot be negative")
    @DecimalMax(value = "100.00", message = "Progress cannot exceed 100.00")
    private BigDecimal progress;

    public ReadingProgressUpdateRequest() {}

    public ReadingProgressUpdateRequest(Integer currentPage, BigDecimal progress) {
        this.currentPage = currentPage;
        this.progress = progress;
    }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }
}

