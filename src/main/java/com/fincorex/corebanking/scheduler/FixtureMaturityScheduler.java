package com.fincorex.corebanking.scheduler;

import com.fincorex.corebanking.entity.FixedDepositDetails;
import com.fincorex.corebanking.enums.FixtureStatus;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.repository.FixtureRepo;
import com.fincorex.corebanking.service.impl.FixtureServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;


public class FixtureMaturityScheduler {

    @Autowired
    private FixtureRepo fixtureRepo;

    @Autowired
    private FixtureServiceImpl fixtureService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processFixtureMaturity() throws BadRequestException, AccountNotFoundException {
        List<FixedDepositDetails> fixedDepositDetailsList = fixtureRepo.findAllByFixtureStatus(FixtureStatus.NORMAL.name());
        for(FixedDepositDetails fixedDepositDetails : fixedDepositDetailsList){
            fixtureService.processBreakageOrMaturityTransaction(fixedDepositDetails);
            fixedDepositDetails.setFixtureStatus(FixtureStatus.MATURED.name());
            fixtureRepo.save(fixedDepositDetails);
        }
    }

}
