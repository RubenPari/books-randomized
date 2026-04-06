package dev.rubenpari.backend.client.isbndb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * ISBNdb list payload for {@code GET /books/{query}} (and similar).
 * OpenAPI names the array {@code data}; live API responses often use {@code books}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IsbnDbBooksResponse {

    private int total;

    @JsonProperty("data")
    @JsonAlias("books")
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
