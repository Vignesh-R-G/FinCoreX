package com.fincorex.corebanking.batch.repaymentcollection;

import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.entity.RepaymentCollectionTag;
import com.fincorex.corebanking.enums.LoanStatus;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepaymentCollectionItemProcessor implements ItemProcessor<RepaymentCollectionTag, RepaymentCollectionTag> {

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;


    @Override
    public RepaymentCollectionTag process(RepaymentCollectionTag repaymentRecord) throws InterruptedException {
        String loanAccountID = repaymentRecord.getLoanAccountID();
        LoanDetails loanDetails = loanDetailsRepo.findById(loanAccountID).get();

        // If completed or settled already, skipping the record
        if(loanDetails.getLoanStatus().equals(LoanStatus.COMPLETED.name()) || loanDetails.getLoanStatus().equals(LoanStatus.SETTLED.name()))
            return null;

        return repaymentRecord;
    }

}
