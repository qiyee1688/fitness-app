package com.fitness.config;

import com.fitness.mapper.ArticleFoodReferenceSchemaMigrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ArticleFoodReferenceSchemaMigrationTest {
    private final ArticleFoodReferenceSchemaMigrationMapper migrationMapper =
            mock(ArticleFoodReferenceSchemaMigrationMapper.class);
    private final ArticleFoodReferenceSchemaMigration migration =
            new ArticleFoodReferenceSchemaMigration(migrationMapper);

    @Test
    void appliesArticleFoodReferencesThroughMyBatisAfterCatalogMigration() {
        migration.run(new DefaultApplicationArguments());

        verify(migrationMapper).apply();
    }
}
