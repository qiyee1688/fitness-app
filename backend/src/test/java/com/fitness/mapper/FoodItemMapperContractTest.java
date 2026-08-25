package com.fitness.mapper;

import com.fitness.domain.FoodCategory;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FoodItemMapperContractTest {
    private Configuration configuration;

    @BeforeEach
    void parseFoodItemMapper() throws Exception {
        configuration = new Configuration();
        String resource = "mapper/FoodItemMapper.xml";
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void listAndCountUseBoundNameCategoryAndPaginationParameters() {
        Map<String, Object> parameters = Map.of(
                "query", "egg",
                "category", FoodCategory.PROTEIN,
                "offset", 20,
                "limit", 10);

        BoundSql listSql = configuration.getMappedStatement("com.fitness.mapper.FoodItemMapper.find")
                .getBoundSql(parameters);
        BoundSql countSql = configuration.getMappedStatement("com.fitness.mapper.FoodItemMapper.count")
                .getBoundSql(parameters);

        assertThat(listSql.getSql())
                .contains("name ILIKE CONCAT('%', ?, '%')")
                .contains("name_en ILIKE CONCAT('%', ?, '%')")
                .contains("category = ?::food_category_enum")
                .contains("ORDER BY category, name_en, id")
                .contains("LIMIT ? OFFSET ?");
        assertThat(listSql.getParameterMappings()).hasSize(5);
        assertThat(countSql.getSql())
                .contains("name ILIKE CONCAT('%', ?, '%')")
                .contains("category = ?::food_category_enum")
                .doesNotContain("LIMIT");
    }

    @Test
    void detailLookupBindsStableBusinessId() {
        BoundSql detailSql = configuration.getMappedStatement("com.fitness.mapper.FoodItemMapper.findById")
                .getBoundSql(Map.of("id", "egg-whole"));

        assertThat(detailSql.getSql()).contains("WHERE id = ?");
        assertThat(detailSql.getParameterMappings()).singleElement()
                .extracting("property")
                .isEqualTo("id");
    }
}
