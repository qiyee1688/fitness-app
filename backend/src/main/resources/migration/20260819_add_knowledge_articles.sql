DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'knowledge_article_status_enum') THEN
        CREATE TYPE knowledge_article_status_enum AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS editors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    display_name VARCHAR(120) NOT NULL,
    display_name_en VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_articles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    editor_id UUID NOT NULL REFERENCES editors(id) ON DELETE RESTRICT,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    title_en VARCHAR(200) NOT NULL,
    summary TEXT NOT NULL,
    summary_en TEXT NOT NULL,
    body TEXT NOT NULL,
    body_en TEXT NOT NULL,
    cover_image_url TEXT,
    status knowledge_article_status_enum NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_published_article_date CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS article_references (
    article_id UUID NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
    exercise_id VARCHAR(64) NOT NULL REFERENCES exercises(id) ON DELETE RESTRICT,
    display_order INT NOT NULL CHECK (display_order > 0),
    PRIMARY KEY (article_id, exercise_id),
    CONSTRAINT uq_article_reference_order UNIQUE (article_id, display_order)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_published
    ON knowledge_articles(published_at DESC, id)
    WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_article_references_exercise_id
    ON article_references(exercise_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger
        WHERE tgname = 'update_knowledge_articles_updated_at'
          AND tgrelid = 'knowledge_articles'::regclass
    ) THEN
        CREATE TRIGGER update_knowledge_articles_updated_at BEFORE UPDATE ON knowledge_articles
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END
$$;
