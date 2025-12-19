package com.fincorex.corebanking.batch.arrearsprocessing;

import com.fincorex.corebanking.entity.ArrearsProcessingTag;
import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.enums.LoanStatus;
import com.fincorex.corebanking.repository.ArrearsProcessingTagRepo;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import com.fincorex.corebanking.service.impl.LoanServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ArrearsProcessingItemWriter implements ItemWriter<ArrearsProcessingTag> {

    @Autowired
    private ArrearsProcessingTagRepo arrearsProcessingTagRepo;

    @Autowired
    private LoanServiceImpl loanService;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    private static final Logger logger = LoggerFactory.getLogger(ArrearsProcessingItemWriter.class);

    public void write(Chunk<? extends ArrearsProcessingTag> arrearsProcessingRecords) throws Exception{
        arrearsProcessingRecords.forEach(arrearsProcessingRecord->{
            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();
            logger.info("Arrears Processing for Record ID: {}, Thread: {} , ThreadID : {}", arrearsProcessingRecord.getRowSequence(), threadName, threadID);
            String loanAccountID = arrearsProcessingRecord.getLoanAccountID();
            LoanDetails loanDetails = loanDetailsRepo.findById(loanAccountID).get();
            loanService.persistMissedRepayments(loanDetails);
            BigDecimal totalArrearsAmount = loanService.fetchTotalUnpaidAmount(loanDetails);


            if(totalArrearsAmount.compareTo(BigDecimal.ZERO) > 0){
                loanDetails.setLoanStatus(LoanStatus.ARREARS.name());
                loanDetailsRepo.save(loanDetails);
            } else {
                loanDetails.setLoanStatus(LoanStatus.NORMAL.name());
                loanDetailsRepo.save(loanDetails);
            }
        });
    }
}

