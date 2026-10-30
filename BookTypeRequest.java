package com.library.dto.booktype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookTypeRequest {

    @NotBlank(message = "Book type name is required")
    @Size(max = 50, message = "Book type name cannot exceed 50 characters")
    private String name;

    private Boolean isActive = true;

    public BookTypeRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

