
-- 1. Enable vector extension (Idempotent)
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Create the standard Spring AI vector store table
-- Note: '768' is the dimension for 'nomic-embed-text-v2', change to 1536 for OpenAI
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(768) --// 1536 is the default embedding dimension
    );

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
