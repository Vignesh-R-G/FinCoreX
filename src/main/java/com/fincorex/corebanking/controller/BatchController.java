package com.fincorex.corebanking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class BatchController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchController.class.getName());

    @Autowired
    @Qualifier("repaymentPartitionedJob")
    private Job repaymentCollection;

    @Autowired
    @Qualifier("arrearsProcessingJob")
    private Job arrearsProcessing;

    @Autowired
    @Qualifier("delinquencyPartitionedJob")
    private Job delinquencyMarking;

    @Autowired
    @Qualifier("interestAccrualJob")
    private Job interestAccrual;

    @Autowired
    @Qualifier("interestApplicationJob")
    private Job interestApplication;

    @Autowired
    private JobLauncher jobLauncher;

    @PostMapping("/repayment/collection")
    @PreAuthorize("hasAuthority('OPERATIONS_OFFICER')")
    public String executeRepaymentCollection() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(repaymentCollection, params);

        while(jobExecution.isRunning()) {
            Thread.sleep(1000);
        }

        LOGGER.info("Job finished with status: {}", jobExecution.getStatus());

        return "Job Completed";
    }

    @PostMapping("/arrears/processing")
    @PreAuthorize("hasAuthority('OPERATIONS_OFFICER')")
    public String executeArrearsProcessing() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(arrearsProcessing, params);

        while(jobExecution.isRunning()) {
            Thread.sleep(1000);
        }

        return "Job Completed";
    }

    @PostMapping("/delinquency/marking")
    @PreAuthorize("hasAuthority('OPERATIONS_OFFICER')")
    public String executeDelinquencyMarking() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(delinquencyMarking, params);


        while(jobExecution.isRunning()) {
            Thread.sleep(1000);
        }

        return "Job Completed";
    }

    @PostMapping("/interest/accrual")
    @PreAuthorize("hasAuthority('OPERATIONS_OFFICER')")
    public String executeInterestAccrual() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(interestAccrual, params);


        while(jobExecution.isRunning()) {
            Thread.sleep(1000);
        }

        return "Job Completed";
    }

    @PostMapping("/interest/application")
    @PreAuthorize("hasAuthority('OPERATIONS_OFFICER')")
    public String executeInterestApplication() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(interestApplication, params);


        while(jobExecution.isRunning()) {
            Thread.sleep(1000);
        }

        return "Job Completed";
    }

}
