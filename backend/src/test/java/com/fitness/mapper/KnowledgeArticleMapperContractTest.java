package com.fitness.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeArticleMapperContractTest {
    private Configuration configuration;

    @BeforeEach
    void parseKnowledgeArticleMapper() throws Exception {
        configuration = new Configuration();
        String resource = "mapper/KnowledgeArticleMapper.xml";
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void articleFoodReferencesBindTheArticleIdAndOnlyReturnPublishedArticleFoods() {
        BoundSql boundSql = configuration
                .getMappedStatement("com.fitness.mapper.KnowledgeArticleMapper.findPublishedFoodItems")
                .getBoundSql(Map.of("articleId", "30000000-0000-0000-0000-000000000001"));

        assertThat(boundSql.getSql())
                .contains("FROM article_food_references afr")
                .contains("WHERE afr.article_id = ?::uuid AND ka.status = 'PUBLISHED'")
                .contains("ORDER BY afr.display_order, fi.id");
        assertThat(boundSql.getParameterMappings()).singleElement()
                .extracting("property")
                .isEqualTo("articleId");
    }
}
