package com.fincorex.corebanking.batch.delinquencymarking;

import com.fincorex.corebanking.entity.DelinquencyMarkingTag;
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
public class DelinquencyMarkingBatchConfig {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private DelinquencyMarkingItemProcessor processor;
    @Autowired
    private DelinquencyMarkingItemWriter writer;

    @Autowired
    private DelinquencyMarkingPartitioner partitioner;


    private final static int CHUNK_SIZE = 10;

    private final static int GRID_SIZE = 10;

    private static final Logger logger = LoggerFactory.getLogger(DelinquencyMarkingBatchConfig.class);



    @Bean(name = "delinquencyPartitionedJob")
    public Job delinquencyPartitionedJob(JobRepository jobRepository, @Qualifier("delinquencyMasterStep") Step masterStep, JobExecutionListener delinquencyPrepListener) {
        return new JobBuilder("delinquencyPartitionedJob", jobRepository)
                .listener(delinquencyPrepListener)
                .start(masterStep)
                .build();
    }

    @Bean
    public JobExecutionListener delinquencyPrepListener(TransactionTemplate txTemplate,
                                                    JdbcTemplate jdbcTemplate) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txTemplate.execute(status -> {

                    String tagDeleteQuery = "delete from bankfusion.delinquencymarkingtag";

                    String tagInsertQuery = "insert into bankfusion.delinquencymarkingtag\n" +
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

    @Bean(name = "delinquencyMasterStep")
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           DelinquencyMarkingPartitioner partitioner, @Qualifier("delinquencySlaveStep") Step slaveStep, @Qualifier("arrearsMasterTaskExecutor") TaskExecutor masterTaskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .gridSize(GRID_SIZE)
                .step(slaveStep)
                .taskExecutor(masterTaskExecutor)
                .build();
    }

    @Bean(name = "delinquencySlaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          @Qualifier("delinquencyReader") JdbcPagingItemReader<DelinquencyMarkingTag> reader,
                          DelinquencyMarkingItemProcessor processor,
                          DelinquencyMarkingItemWriter writer) {

        return new StepBuilder("slaveStep", jobRepository)
                .<DelinquencyMarkingTag, DelinquencyMarkingTag>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "delinquencyReader")
    @StepScope
    public JdbcPagingItemReader<DelinquencyMarkingTag> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId
    ) {
        JdbcPagingItemReader<DelinquencyMarkingTag> reader = new JdbcPagingItemReader<>();
        reader.setName("DelinquencyMarkingTag_paging_item_reader");
        reader.setDataSource(dataSource);
        reader.setFetchSize(CHUNK_SIZE);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT row_sequence,loan_accountid");
        queryProvider.setFromClause("FROM bankfusion.delinquencymarkingtag");
        queryProvider.setWhereClause("WHERE row_sequence >= :minId AND row_sequence <= :maxId");
        queryProvider.setSortKey("row_sequence");
        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set parameters for minId and maxId from partition context
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minId", minId);
        parameterValues.put("maxId", maxId);
        reader.setParameterValues(parameterValues);

        // Inline RowMapper lambda like your example
        reader.setRowMapper((rs, rowNum) -> {
            DelinquencyMarkingTag delinquencyMarkingTag = new DelinquencyMarkingTag();
            delinquencyMarkingTag.setRowSequence(rs.getLong("row_sequence"));
            delinquencyMarkingTag.setLoanAccountID(rs.getString("loan_accountid"));
            return delinquencyMarkingTag;
        });

        logger.info("ROWS FETCHED FROM : {} TO :{}", minId, maxId);

        return reader;
    }

    @Bean(name = "delinquencyMasterTaskExecutor")
    public TaskExecutor repaymentMasterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("partition-thread-");
        executor.initialize();
        return executor;
    }

}