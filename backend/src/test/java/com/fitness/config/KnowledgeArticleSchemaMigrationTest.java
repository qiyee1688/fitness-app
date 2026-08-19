package com.fitness.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeArticleSchemaMigrationTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final KnowledgeArticleSchemaMigration migration = new KnowledgeArticleSchemaMigration(jdbcTemplate);

    @Test
    void appliesIdempotentArticleSchema() throws Exception {
        migration.run(new DefaultApplicationArguments());

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getValue())
                .contains("CREATE TABLE IF NOT EXISTS editors")
                .contains("CREATE TABLE IF NOT EXISTS knowledge_articles")
                .contains("CREATE TABLE IF NOT EXISTS article_references")
                .contains("status <> 'PUBLISHED' OR published_at IS NOT NULL");
    }
}
