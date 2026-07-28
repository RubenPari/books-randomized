package com.booksrandomized.backend.catalog;

import java.util.List;

public interface CatalogClient {
    List<Book> search(String query, int limit);

    default List<Book> searchBatch(String query, int limit, int batch) {
        return search(query, limit);
    }
}
