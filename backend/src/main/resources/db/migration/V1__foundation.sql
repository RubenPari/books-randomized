CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens(user_id);

CREATE TABLE reading_list_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    catalog_book_id VARCHAR(100) NOT NULL,
    status VARCHAR(24) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, catalog_book_id)
);

CREATE TABLE discovered_books (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    catalog_book_id VARCHAR(100) NOT NULL,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, catalog_book_id)
);

CREATE TABLE recommendation_feedback (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    catalog_book_id VARCHAR(100) NOT NULL,
    sentiment VARCHAR(16) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX recommendation_feedback_user_id_idx ON recommendation_feedback(user_id);

CREATE TABLE catalog_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    normalized_payload TEXT NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX catalog_cache_expires_at_idx ON catalog_cache(expires_at);
