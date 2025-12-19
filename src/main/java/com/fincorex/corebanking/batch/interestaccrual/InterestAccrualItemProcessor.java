package com.fincorex.corebanking.batch.interestaccrual;

import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.InterestAccrualTag;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.utils.InterestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InterestAccrualItemProcessor implements ItemProcessor<InterestAccrualTag, InterestAccrualTag> {

    @Autowired
    private AccountRepo accountRepo;

    private static final Logger logger = LoggerFactory.getLogger(InterestAccrualItemProcessor.class);


    @Override
    public InterestAccrualTag process(InterestAccrualTag interestAccrualRecord) throws InterruptedException {
        String accountID = interestAccrualRecord.getAccountID();

        Account account = accountRepo.findById(accountID).get();

        if(!InterestUtils.isInterestAccrualProduct(account.getSubProduct()))
            return null;

        return interestAccrualRecord;
    }

}
