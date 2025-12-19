package com.fincorex.corebanking.batch.arrearsprocessing;

import com.fincorex.corebanking.entity.ArrearsProcessingTag;
import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.enums.LoanStatus;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArrearsProcessingItemProcessor implements ItemProcessor<ArrearsProcessingTag, ArrearsProcessingTag> {

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Override
    public ArrearsProcessingTag process(ArrearsProcessingTag arrearsProcessingRecord) throws InterruptedException {
        LoanDetails loanDetails = loanDetailsRepo.findById(arrearsProcessingRecord.getLoanAccountID()).get();

        if(loanDetails.getLoanStatus().equals(LoanStatus.COMPLETED.name()) || loanDetails.getLoanStatus().equals(LoanStatus.SETTLED.name()))
            return null;
        return arrearsProcessingRecord;
    }

}
