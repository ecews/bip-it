CREATE TABLE IF NOT EXISTS document_status
(
    id            SERIAL PRIMARY KEY,
    document_name VARCHAR(255) UNIQUE NOT NULL,
    is_populated  BOOLEAN             NOT NULL DEFAULT FALSE
);

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
                                            id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
                                            content text,
                                            metadata json,
                                            embedding vector(384)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

