package com.fincorex.corebanking.batch.delinquencymarking;

import com.fincorex.corebanking.entity.DelinquencyMarkingTag;
import com.fincorex.corebanking.entity.DelinquencyStage;
import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.entity.LoanRepayments;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import com.fincorex.corebanking.repository.LoanRepaymentsRepo;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DelinquencyMarkingItemProcessor implements ItemProcessor<DelinquencyMarkingTag, DelinquencyMarkingTag> {

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Autowired
    private LoanRepaymentsRepo loanRepaymentsRepo;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    private static final Logger logger = LoggerFactory.getLogger(DelinquencyMarkingItemProcessor.class);


    @Override
    public DelinquencyMarkingTag process(DelinquencyMarkingTag delinquencyMarkingRecord) throws InterruptedException {
        try {
            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();
            logger.info("Arrears Processing for Record ID: {}, Thread: {} , ThreadID : {}", delinquencyMarkingRecord.getRowSequence(), threadName, threadID);
            String loanAccountID = delinquencyMarkingRecord.getLoanAccountID();
            LoanDetails loanDetails = loanDetailsRepo.findById(loanAccountID).get();

            // OverDue Days Calculation
            long overDueDays = calculateOverDueDays(loanDetails);


            // Delinquency Stage Marking Process
            List<DelinquencyStage> delinquencyStages = loanDetails.getDelinquencyFeature().getDelinquencyStages();
            long maxOverDueDaysReached = 0;
            DelinquencyStage delinquencyStageToBeMarked = null;
            for (DelinquencyStage delinquencyStage : delinquencyStages) {
                if (overDueDays >= delinquencyStage.getOverDueDays() && overDueDays > maxOverDueDaysReached) {
                    maxOverDueDaysReached = delinquencyStage.getOverDueDays();
                    delinquencyStageToBeMarked = delinquencyStage;
                }
            }

            loanDetails.setDelinquencyStage(delinquencyStageToBeMarked);
            loanDetailsRepo.save(loanDetails);


        } catch (Exception exception){
            logger.error("Error occurred on the delinquency marking processing for the Loan Account ID : {}" ,delinquencyMarkingRecord.getLoanAccountID());
            return null;
        }

        return delinquencyMarkingRecord;
    }

    private long calculateOverDueDays(LoanDetails loanDetails){
        List<LoanRepayments> loanRepayments = loanRepaymentsRepo.findAllByLoanDetailsOrderByRepaymentNumberAsc(loanDetails);
        for(LoanRepayments loanRepayment : loanRepayments){
            if(loanRepayment.getTotalRepaymentOverDue().compareTo(BigDecimal.ZERO) > 0){
                return ChronoUnit.DAYS.between(loanRepayment.getRepaymentDate().toLocalDate(), businessDateUtil.getCurrentBusinessDate().toLocalDate());
            }
        }
        return 0;
    }
}
