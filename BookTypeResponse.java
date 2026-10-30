package com.library.dto.booktype;

public class BookTypeResponse {

    private Long id;
    private String name;
    private Boolean isActive;

    public BookTypeResponse() {}

    public BookTypeResponse(Long id, String name, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

