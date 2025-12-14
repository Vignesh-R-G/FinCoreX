package com.fincorex.corebanking.batch.interestapplication;

import com.fincorex.corebanking.entity.InterestApplicationTag;
import com.fincorex.corebanking.utils.BusinessDateUtil;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableBatchProcessing
public class InterestApplicationBatchConfig {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private InterestApplicationItemProcessor processor;
    @Autowired
    private InterestApplicationItemWriter writer;


    private final static int CHUNK_SIZE = 10;

    private final static int GRID_SIZE = 10;

    private static final Logger logger = LoggerFactory.getLogger(InterestApplicationBatchConfig.class);

    @Bean(name = "interestApplicationJob")
    public Job interestApplicationJob(JobRepository jobRepository, @Qualifier("interestApplicationMasterStep") Step masterStep, JobExecutionListener interestApplicationPrepListener) {
        return new JobBuilder("interestApplicationJob", jobRepository)
                .listener(interestApplicationPrepListener)
                .start(masterStep)
                .build();
    }

    @Bean
    public JobExecutionListener interestApplicationPrepListener(TransactionTemplate txTemplate, JdbcTemplate jdbcTemplate, BusinessDateUtil businessDateUtil) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txTemplate.execute(status -> {

                    String tagDeleteQuery = "delete from bankfusion.interestapplicationtag";

                    String tagInsertQuery = "insert into bankfusion.interestapplicationtag \n" +
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

                    // Only on End of the Month, Interest Application will be executed
                    LocalDate endOfMonth = YearMonth.from(businessDateUtil.getCurrentBusinessDate().toLocalDate()).atEndOfMonth();
                    if(businessDateUtil.getCurrentBusinessDate().toLocalDate().isEqual(endOfMonth)) {
                        jdbcTemplate.update(tagDeleteQuery);
                        jdbcTemplate.update(tagInsertQuery);
                    }
                    return null;
                });
            }
        };
    }

    @Bean(name = "interestApplicationMasterStep")
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           InterestApplicationPartitioner partitioner, @Qualifier("interestApplicationSlaveStep") Step slaveStep, @Qualifier("interestApplicationMasterTaskExecutor") TaskExecutor masterTaskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .gridSize(GRID_SIZE)
                .step(slaveStep)
                .taskExecutor(masterTaskExecutor)
                .build();
    }

    @Bean(name = "interestApplicationSlaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          @Qualifier("interestApplicationReader") JdbcPagingItemReader<InterestApplicationTag> reader,
                          InterestApplicationItemProcessor processor,
                          InterestApplicationItemWriter writer,
                          @Qualifier("interestApplicationMasterTaskExecutor") TaskExecutor masterTaskExecutor) {

        return new StepBuilder("slaveStep", jobRepository)
                .<InterestApplicationTag, InterestApplicationTag>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "interestApplicationReader")
    @StepScope
    public JdbcPagingItemReader<InterestApplicationTag> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId
    ) {
        JdbcPagingItemReader<InterestApplicationTag> reader = new JdbcPagingItemReader<>();
        reader.setName("InterestApplicationTag_paging_item_reader");
        reader.setDataSource(dataSource);
        reader.setFetchSize(CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT row_sequence,accountid");
        queryProvider.setFromClause("FROM bankfusion.interestapplicationtag");
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
            InterestApplicationTag interestApplicationTag = new InterestApplicationTag();
            interestApplicationTag.setRowSequence(rs.getLong("row_sequence"));
            interestApplicationTag.setAccountID(rs.getString("accountid"));
            return interestApplicationTag;
        });

        return reader;
    }

    @Bean(name = "interestApplicationMasterTaskExecutor")
    public TaskExecutor interestApplicationMasterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("partition-thread-");
        executor.initialize();
        return executor;
    }

}