CREATE TABLE books (
    id                 BIGSERIAL    PRIMARY KEY,
    title              VARCHAR(255) NOT NULL,
    price              INT          NOT NULL CHECK (price >= 0),
    publication_status VARCHAR(20)  NOT NULL DEFAULT 'UNPUBLISHED'
                           CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED')),
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
