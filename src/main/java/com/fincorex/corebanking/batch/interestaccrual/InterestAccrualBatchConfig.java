package com.fincorex.corebanking.batch.interestaccrual;

import com.fincorex.corebanking.entity.InterestAccrualTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableBatchProcessing
public class InterestAccrualBatchConfig {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private InterestAccrualItemProcessor processor;
    @Autowired
    private InterestAccrualItemWriter writer;


    private final static int CHUNK_SIZE = 10;

    private final static int GRID_SIZE = 10;

    private static final Logger logger = LoggerFactory.getLogger(InterestAccrualBatchConfig.class);

    @Bean(name = "interestAccrualJob")
    public Job interestAccrualJob(JobRepository jobRepository, @Qualifier("interestAccrualMasterStep") Step masterStep, JobExecutionListener interestAccrualPrepListener) {
        return new JobBuilder("interestAccrualJob", jobRepository)
                .listener(interestAccrualPrepListener)
                .start(masterStep)
                .build();
    }

    @Bean
    public JobExecutionListener interestAccrualPrepListener(TransactionTemplate txTemplate,
                                                    JdbcTemplate jdbcTemplate) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txTemplate.execute(status -> {

                    String tagDeleteQuery = "delete from bankfusion.interestaccrualtag";

                    String tagInsertQuery = "insert into bankfusion.interestaccrualtag \n" +
                            "(accountid, row_sequence)\n" +
                            "select acc.accountid, distacc.rnum from\n" +
                            "bankfusion.account  acc\n" +
                            "inner join bankfusion.product_inheritance prod\n" +
                            "on prod.product_context_code = acc.sub_product_id\n" +
                            "inner join\n" +
                            "(\n" +
                            "  select ROW_NUMBER() over (order by gl_account) as rnum,gl_account from(\n" +
                            "    select distinct gl_account\n" +
                            "    from bankfusion.product_inheritance\n" +
                            "  ) \n" +
                            ")\n" +
                            "distacc \n" +
                            "on prod.gl_account = distacc.gl_account";

                    jdbcTemplate.update(tagDeleteQuery);
                    jdbcTemplate.update(tagInsertQuery);
                    return null;
                });
            }
        };
    }

    @Bean(name = "interestAccrualMasterStep")
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           InterestAccrualPartitioner partitioner, @Qualifier("interestAccrualSlaveStep") Step slaveStep, @Qualifier("interestAccrualMasterTaskExecutor") TaskExecutor masterTaskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .gridSize(GRID_SIZE)
                .step(slaveStep)
                .taskExecutor(masterTaskExecutor)
                .build();
    }

    @Bean(name = "interestAccrualSlaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          @Qualifier("interestAccrualReader") JdbcPagingItemReader<InterestAccrualTag> reader,
                          InterestAccrualItemProcessor processor,
                          InterestAccrualItemWriter writer,
                          @Qualifier("interestAccrualMasterTaskExecutor") TaskExecutor masterTaskExecutor) {

        return new StepBuilder("slaveStep", jobRepository)
                .<InterestAccrualTag, InterestAccrualTag>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "interestAccrualReader")
    @StepScope
    public JdbcPagingItemReader<InterestAccrualTag> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId
    ) {
        JdbcPagingItemReader<InterestAccrualTag> reader = new JdbcPagingItemReader<>();
        reader.setName("InterestAccrualTag_paging_item_reader");
        reader.setDataSource(dataSource);
        reader.setFetchSize(CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT row_sequence,accountid");
        queryProvider.setFromClause("FROM bankfusion.interestaccrualtag");
        queryProvider.setWhereClause("WHERE row_sequence >= :minId AND row_sequence <= :maxId");
        queryProvider.setSortKey("row_sequence");
        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minId", minId);
        parameterValues.put("maxId", maxId);
        reader.setParameterValues(parameterValues);

        // Inline RowMapper lambda like your example
        reader.setRowMapper((rs, rowNum) -> {
            InterestAccrualTag interestAccrualTag = new InterestAccrualTag();
            interestAccrualTag.setRowSequence(rs.getLong("row_sequence"));
            interestAccrualTag.setAccountID(rs.getString("accountid"));
            return interestAccrualTag;
        });

        return reader;
    }

    @Bean(name = "interestAccrualMasterTaskExecutor")
    public TaskExecutor interestAccrualMasterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("partition-thread-");
        executor.initialize();
        return executor;
    }

}