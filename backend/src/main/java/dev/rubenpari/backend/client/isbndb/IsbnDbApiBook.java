package dev.rubenpari.backend.client.isbndb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * ISBNdb API v2 {@code Book} schema subset (see https://api2.isbndb.com/doc.json).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IsbnDbApiBook {

    private String title;

    /** Deprecated in spec but still returned by some records. */
    @JsonProperty("isbn")
    private String isbnLegacy;

    @JsonProperty("isbn13")
    private String isbn13;

    @JsonProperty("isbn10")
    private String isbn10;

    private List<String> authors;
    private List<String> subjects;

    private String language;

    private String synopsis;
    private String excerpt;

    private String image;

    @JsonProperty("date_published")
    private String datePublished;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbnLegacy() {
        return isbnLegacy;
    }

    public void setIsbnLegacy(String isbnLegacy) {
        this.isbnLegacy = isbnLegacy;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }
}
