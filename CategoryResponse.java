package com.library.dto.category;

public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private Boolean isActive;
    private long bookCount;

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, String description, String icon, Boolean isActive, long bookCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
        this.bookCount = bookCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public long getBookCount() { return bookCount; }
    public void setBookCount(long bookCount) { this.bookCount = bookCount; }
}

