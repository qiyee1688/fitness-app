package com.fitness.config;

import com.fitness.mapper.FoodItemSchemaMigrationMapper;
import com.fitness.mapper.FoodItemSchemaMigrationTestMapper;
import com.fitness.mapper.FoodItemConstraintTestRow;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.Reader;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class FoodItemSchemaMigrationPostgreSqlTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void applyFoodItemMigration() throws Exception {
        DataSource dataSource = new UnpooledDataSource(
                "org.postgresql.Driver",
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
        Configuration configuration = new Configuration(
                new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.addMapper(FoodItemSchemaMigrationMapper.class);
        configuration.addMapper(FoodItemSchemaMigrationTestMapper.class);
        parseMapper(configuration, "mapper/FoodItemSchemaMigrationMapper.xml");
        parseMapper(configuration, "mapper/FoodItemSchemaMigrationTestMapper.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            sqlSession.getMapper(FoodItemSchemaMigrationTestMapper.class).createUpdatedAtFunction();
            new FoodItemSchemaMigration(sqlSession.getMapper(FoodItemSchemaMigrationMapper.class))
                    .run(new DefaultApplicationArguments());
            sqlSession.commit();
        }
    }

    @Test
    void rejectsNonPositiveServingAndNegativeMacrosAfterApplyingTheMigration() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            assertThat(sqlSession.getMapper(FoodItemSchemaMigrationTestMapper.class).countFoodItems())
                    .isEqualTo(5);
        }

        assertConstraintViolation(new FoodItemConstraintTestRow(
                "invalid-serving", BigDecimal.ZERO, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
        assertConstraintViolation(new FoodItemConstraintTestRow(
                "invalid-protein", new BigDecimal("100"), new BigDecimal("-1"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
    }

    private void parseMapper(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    private void assertConstraintViolation(FoodItemConstraintTestRow row) {
        assertThatThrownBy(() -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                sqlSession.getMapper(FoodItemSchemaMigrationTestMapper.class).insertFoodItem(row);
                sqlSession.commit();
            }
        }).isInstanceOf(org.apache.ibatis.exceptions.PersistenceException.class);
    }
}
