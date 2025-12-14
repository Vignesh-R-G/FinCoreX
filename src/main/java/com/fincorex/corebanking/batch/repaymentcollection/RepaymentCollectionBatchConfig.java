package com.fincorex.corebanking.batch.repaymentcollection;

import com.fincorex.corebanking.entity.RepaymentCollectionTag;
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
public class RepaymentCollectionBatchConfig {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private RepaymentCollectionItemProcessor processor;
    @Autowired
    private RepaymentCollectionItemWriter writer;

    @Autowired
    private RepaymentCollectionPartitioner partitioner;


    private final static int CHUNK_SIZE = 10;

    private final static int GRID_SIZE = 10;


    private static final Logger logger = LoggerFactory.getLogger(RepaymentCollectionBatchConfig.class);



    @Bean(name = "repaymentPartitionedJob")
    public Job repaymentPartitionedJob(JobRepository jobRepository, @Qualifier("repaymentMasterStep") Step masterStep, JobExecutionListener repaymentPrepListener) {
        return new JobBuilder("repaymentPartitionedJob", jobRepository)
                .start(masterStep)
                .listener(repaymentPrepListener)
                .build();
    }

    @Bean
    public JobExecutionListener repaymentPrepListener(TransactionTemplate txTemplate,
                                                    JdbcTemplate jdbcTemplate) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txTemplate.execute(status -> {

                    String tagDeleteQuery = "delete from bankfusion.repaymentcollectiontag";

                    String tagInsertQuery = "insert into bankfusion.repaymentcollectiontag\n" +
                            "(loan_accountid, settlement_accountid, row_sequence)\n" +
                            "select ld.loan_accountid, ld.settlement_account_id, ds.rnum from bankfusion.loan_details ld\n" +
                            "inner join\n" +
                            "(\n" +
                            "  select ROW_NUMBER() over (order by settlement_account_id) as rnum,settlement_account_id from(\n" +
                            "    select distinct settlement_account_id\n" +
                            "    from bankfusion.loan_details\n" +
                            "  ) \n" +
                            ") ds\n" +
                            "on ld.settlement_account_id = ds.settlement_account_id";

                    jdbcTemplate.update(tagDeleteQuery);
                    jdbcTemplate.update(tagInsertQuery);
                    return null;
                });
            }
        };
    }

    @Bean(name = "repaymentMasterStep")
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           RepaymentCollectionPartitioner partitioner, @Qualifier("repaymentSlaveStep") Step slaveStep, @Qualifier("repaymentMasterTaskExecutor") TaskExecutor masterTaskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .gridSize(GRID_SIZE)
                .step(slaveStep)
                .taskExecutor(masterTaskExecutor)
                .build();
    }

    @Bean(name = "repaymentSlaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          @Qualifier("repaymentReader") JdbcPagingItemReader<RepaymentCollectionTag> reader,
                          RepaymentCollectionItemProcessor processor,
                          RepaymentCollectionItemWriter writer) {

        return new StepBuilder("slaveStep", jobRepository)
                .<RepaymentCollectionTag, RepaymentCollectionTag>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "repaymentReader")
    @StepScope
    public JdbcPagingItemReader<RepaymentCollectionTag> reader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId
    ) {
        JdbcPagingItemReader<RepaymentCollectionTag> reader = new JdbcPagingItemReader<>();
        reader.setName("RepaymentCollectionTag_paging_item_reader");
        reader.setDataSource(dataSource);
        reader.setFetchSize(10);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT row_sequence,loan_accountid,settlement_accountid");
        queryProvider.setFromClause("FROM bankfusion.repaymentcollectiontag");
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
            RepaymentCollectionTag repaymentCollectionTag = new RepaymentCollectionTag();
            repaymentCollectionTag.setLoanAccountID(rs.getString("loan_accountid"));
            repaymentCollectionTag.setSettlementAccountID(rs.getString("settlement_accountid"));
            return repaymentCollectionTag;
        });

        logger.info("ROWS FETCHED FROM : {} TO :{}", minId, maxId);

        return reader;
    }

    @Bean(name = "repaymentMasterTaskExecutor")
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