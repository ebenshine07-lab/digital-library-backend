package com.library.dto.dashboard;

import com.library.dto.book.BookSummaryResponse;
import com.library.dto.category.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardResponse {

    private List<BookSummaryResponse> previousReading = new ArrayList<>();
    private List<CategoryResponse> subjects = new ArrayList<>();
    private List<BookSummaryResponse> newBooks = new ArrayList<>();
    private List<BookSummaryResponse> popularBooks = new ArrayList<>();
    private List<AuthorSummaryDto> writersAndAuthors = new ArrayList<>();

    public UserDashboardResponse() {}

    public List<BookSummaryResponse> getPreviousReading() { return previousReading; }
    public void setPreviousReading(List<BookSummaryResponse> previousReading) { this.previousReading = previousReading; }

    public List<CategoryResponse> getSubjects() { return subjects; }
    public void setSubjects(List<CategoryResponse> subjects) { this.subjects = subjects; }

    public List<BookSummaryResponse> getNewBooks() { return newBooks; }
    public void setNewBooks(List<BookSummaryResponse> newBooks) { this.newBooks = newBooks; }

    public List<BookSummaryResponse> getPopularBooks() { return popularBooks; }
    public void setPopularBooks(List<BookSummaryResponse> popularBooks) { this.popularBooks = popularBooks; }

    public List<AuthorSummaryDto> getWritersAndAuthors() { return writersAndAuthors; }
    public void setWritersAndAuthors(List<AuthorSummaryDto> writersAndAuthors) { this.writersAndAuthors = writersAndAuthors; }
}

