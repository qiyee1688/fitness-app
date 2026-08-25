package com.fitness.mapper;

import com.fitness.domain.PrescriptionAdjustment;
import com.fitness.domain.PrescriptionAdjustmentStatus;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PrescriptionAdjustmentConcurrencyPostgreSqlTest {
    private static final String USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PLAN_ID = "22222222-2222-2222-2222-222222222222";
    private static final String ADJUSTMENT_ID = "33333333-3333-3333-3333-333333333333";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUpDatabase() throws Exception {
        DataSource dataSource = new UnpooledDataSource(
                "org.postgresql.Driver",
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
        Configuration configuration = new Configuration(
                new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.addMapper(PrescriptionAdjustmentMapper.class);
        configuration.addMapper(PrescriptionAdjustmentConcurrencyTestMapper.class);
        parseMapper(configuration, "mapper/PrescriptionAdjustmentMapper.xml");
        parseMapper(configuration, "mapper/PrescriptionAdjustmentConcurrencyTestMapper.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            PrescriptionAdjustmentConcurrencyTestMapper mapper = session
                    .getMapper(PrescriptionAdjustmentConcurrencyTestMapper.class);
            mapper.dropAdjustments();
            mapper.dropPlans();
            mapper.dropAdjustmentStatusType();
            mapper.dropPlanStatusType();
            mapper.createPlanStatusType();
            mapper.createAdjustmentStatusType();
            mapper.createPlans();
            mapper.createAdjustments();
            mapper.insertPlan(PLAN_ID, USER_ID);
            mapper.insertPendingAdjustment(ADJUSTMENT_ID, PLAN_ID);
            session.commit();
        }
    }

    @Test
    void serializesCompetingResolutionRequestsAndReturnsThePersistedWinner() throws Exception {
        CountDownLatch secondRequestStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SqlSession firstSession = sqlSessionFactory.openSession(false)) {
            PrescriptionAdjustmentMapper firstMapper = firstSession.getMapper(PrescriptionAdjustmentMapper.class);
            PrescriptionAdjustment first = firstMapper.findOwnedById(ADJUSTMENT_ID, USER_ID);
            assertThat(first.getStatus()).isEqualTo(PrescriptionAdjustmentStatus.PENDING);

            Future<PrescriptionAdjustment> secondRequest = executor.submit(() -> {
                secondRequestStarted.countDown();
                try (SqlSession secondSession = sqlSessionFactory.openSession(false)) {
                    PrescriptionAdjustment result = secondSession
                            .getMapper(PrescriptionAdjustmentMapper.class)
                            .findOwnedById(ADJUSTMENT_ID, USER_ID);
                    secondSession.commit();
                    return result;
                }
            });

            assertThat(secondRequestStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(secondRequest.isDone()).isFalse();
            assertThat(firstMapper.resolvePending(
                    ADJUSTMENT_ID, PrescriptionAdjustmentStatus.ACCEPTED, LocalDateTime.now())).isEqualTo(1);
            firstSession.commit();

            assertThat(secondRequest.get(3, TimeUnit.SECONDS).getStatus())
                    .isEqualTo(PrescriptionAdjustmentStatus.ACCEPTED);
        } finally {
            executor.shutdownNow();
        }
    }

    private void parseMapper(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }
}
