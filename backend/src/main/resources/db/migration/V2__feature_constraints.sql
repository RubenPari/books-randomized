ALTER TABLE recommendation_feedback
    ADD CONSTRAINT recommendation_feedback_user_book_unique UNIQUE (user_id, catalog_book_id);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE discovered_book_subjects (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    catalog_book_id VARCHAR(100) NOT NULL,
    subject VARCHAR(120) NOT NULL,
    PRIMARY KEY (user_id, catalog_book_id, subject)
);
