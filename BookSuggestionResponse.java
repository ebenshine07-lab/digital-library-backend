package com.library.dto.book;

import java.util.ArrayList;
import java.util.List;

public class BookSuggestionResponse {

    private List<String> titles = new ArrayList<>();
    private List<String> authors = new ArrayList<>();

    public BookSuggestionResponse() {}

    public BookSuggestionResponse(List<String> titles, List<String> authors) {
        this.titles = titles != null ? titles : new ArrayList<>();
        this.authors = authors != null ? authors : new ArrayList<>();
    }

    public List<String> getTitles() { return titles; }
    public void setTitles(List<String> titles) { this.titles = titles; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
}

