CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE incidents ADD COLUMN embedding vector(384);

CREATE INDEX idx_incidents_embedding ON incidents
    USING hnsw (embedding vector_cosine_ops);