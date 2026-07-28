package com.booksrandomized.backend.catalog;

public final class BookIds {
    private BookIds() {}

    public static String canonicalize(String raw) {
        if (raw == null) {
            return null;
        }
        String id = raw.trim();
        if (id.startsWith("/works/")) {
            id = id.substring("/works/".length());
        }
        return id;
    }
}
