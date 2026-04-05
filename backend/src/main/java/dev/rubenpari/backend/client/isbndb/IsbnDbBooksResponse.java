package dev.rubenpari.backend.client.isbndb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ISBNdb {@code GetBooksMultipleResponse} for {@code GET /books/{query}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IsbnDbBooksResponse {

    private int total;
    private List<IsbnDbApiBook> data = new ArrayList<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<IsbnDbApiBook> getData() {
        return data;
    }

    public void setData(List<IsbnDbApiBook> data) {
        this.data = data != null ? data : new ArrayList<>();
    }
}
