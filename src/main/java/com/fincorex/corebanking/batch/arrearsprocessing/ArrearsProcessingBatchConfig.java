package com.fincorex.corebanking.batch.arrearsprocessing;

import com.fincorex.corebanking.entity.ArrearsProcessingTag;
import com.fincorex.corebanking.exception.BadRequestException;
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
public class ArrearsProcessingBatchConfig {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ArrearsProcessingItemProcessor processor;
    @Autowired
    private ArrearsProcessingItemWriter writer;


    private final static int CHUNK_SIZE = 10;

    private final static int GRID_SIZE = 10;

    private static final Logger logger = LoggerFactory.getLogger(ArrearsProcessingBatchConfig.class);

    @Bean(name = "arrearsProcessingJob")
    public Job arrearsJob(JobRepository jobRepository, @Qualifier("arrearsMasterStep") Step masterStep, JobExecutionListener arrearsPrepListener) {
        return new JobBuilder("arrearsPartitionedJob", jobRepository)
                .listener(arrearsPrepListener)
                .start(masterStep)
                .build();
    }

    @Bean
    public JobExecutionListener arrearsPrepListener(TransactionTemplate txTemplate,
                                                    JdbcTemplate jdbcTemplate) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txTemplate.execute(status -> {

                    String tagDeleteQuery = "delete from bankfusion.arrearsprocessingtag";

                    String tagInsertQuery = "insert into bankfusion.arrearsprocessingtag\n" +
                            "(row_sequence,loan_accountid)\n" +
                            "select ROW_NUMBER() over (order by loan_accountid) AS rownum,\n" +
                            "       loan_accountid\n" +
                            "from bankfusion.loan_details";

                    jdbcTemplate.update(tagDeleteQuery);
                    jdbcTemplate.update(tagInsertQuery);
                    return null;
                });
            }
        };
    }

    @Bean(name = "arrearsMasterStep")
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           ArrearsProcessingPartitioner partitioner, @Qualifier("arrearsSlaveStep") Step slaveStep, @Qualifier("arrearsMasterTaskExecutor") TaskExecutor masterTaskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .gridSize(GRID_SIZE)
                .step(slaveStep)
                .taskExecutor(masterTaskExecutor)
                .build();
    }

    @Bean(name = "arrearsSlaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          @Qualifier("arrearsReader") JdbcPagingItemReader<ArrearsProcessingTag> reader,
                          ArrearsProcessingItemProcessor processor,
                          ArrearsProcessingItemWriter writer,
                          @Qualifier("arrearsMasterTaskExecutor") TaskExecutor masterTaskExecutor) {

        return new StepBuilder("slaveStep", jobRepository)
                .<ArrearsProcessingTag, ArrearsProcessingTag>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(BadRequestException.class)
                .skipLimit(10)   // allow up to 10 skips in this step
                .build();
    }

    @Bean(name = "arrearsReader")
    @StepScope
    public JdbcPagingItemReader<ArrearsProcessingTag> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId
    ) {
        JdbcPagingItemReader<ArrearsProcessingTag> reader = new JdbcPagingItemReader<>();
        reader.setName("ArrearsProcessingTag_paging_item_reader");
        reader.setDataSource(dataSource);
        reader.setFetchSize(CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT row_sequence,loan_accountid");
        queryProvider.setFromClause("FROM bankfusion.arrearsprocessingtag");
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
            ArrearsProcessingTag arrearsProcessingTag = new ArrearsProcessingTag();
            arrearsProcessingTag.setRowSequence(rs.getLong("row_sequence"));
            arrearsProcessingTag.setLoanAccountID(rs.getString("loan_accountid"));
            return arrearsProcessingTag;
        });

        return reader;
    }

    @Bean(name = "arrearsMasterTaskExecutor")
    public TaskExecutor arrearsMasterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("partition-thread-");
        executor.initialize();
        return executor;
    }

}