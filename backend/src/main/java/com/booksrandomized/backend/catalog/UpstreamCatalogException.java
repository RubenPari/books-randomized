package com.booksrandomized.backend.catalog;

public final class UpstreamCatalogException extends RuntimeException {
    private final boolean timeout;

    private UpstreamCatalogException(String message, boolean timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    public static UpstreamCatalogException unavailable(Throwable cause) {
        return new UpstreamCatalogException("The catalog service is temporarily unavailable", false, cause);
    }

    public static UpstreamCatalogException timeout(Throwable cause) {
        return new UpstreamCatalogException("The catalog service timed out", true, cause);
    }

    public boolean isTimeout() {
        return timeout;
    }
}
