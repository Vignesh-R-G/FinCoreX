package com.fincorex.corebanking.batch.repaymentcollection;

import com.fincorex.corebanking.dto.BlockTransactionDTO;
import com.fincorex.corebanking.dto.UnBlockTransactionDTO;
import com.fincorex.corebanking.entity.*;
import com.fincorex.corebanking.enums.LoanStatus;
import com.fincorex.corebanking.repository.*;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.service.impl.LoanServiceImpl;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

@Component
public class RepaymentCollectionItemWriter implements ItemWriter<RepaymentCollectionTag> {

    @Autowired
    private RepaymentCollectionTagRepo repaymentCollectionTagRepo;

    @Autowired
    private LoanServiceImpl loanService;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private LoanScheduleRepo loanScheduleRepo;

    @Autowired
    private LendingBlocksRepo lendingBlocksRepo;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    private static final Logger logger = LoggerFactory.getLogger(RepaymentCollectionItemWriter.class);


    public void write(Chunk<? extends RepaymentCollectionTag> repaymentRecords) throws Exception{
        for (RepaymentCollectionTag repaymentRecord : repaymentRecords) {
            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();
            logger.info("Processing Repayment For Loan Account ID: {}, Thread: {} , ThreadID : {}", repaymentRecord.getLoanAccountID(), threadName, threadID);
            String loanAccountID = repaymentRecord.getLoanAccountID();
            String settlementAccountID = repaymentRecord.getSettlementAccountID();
            LoanDetails loanDetails = loanDetailsRepo.findById(loanAccountID).get();
            long lookAheadDays = loanDetails.getLookAheadDays();
            BigDecimal blockAmount = BigDecimal.ZERO;
            Date businessDate = businessDateUtil.getCurrentBusinessDate();

            // Unblock Existing Block
            Optional<LendingBlocks> lendingBlocksOptional = lendingBlocksRepo.findById(loanAccountID);
            if (lendingBlocksOptional.isPresent()) {
                LendingBlocks lendingBlocks = lendingBlocksOptional.get();
                UnBlockTransactionDTO unBlockTransactionDTO = UnBlockTransactionDTO.builder()
                        .accountID(lendingBlocks.getSettlementAccountID())
                        .unBlockAmount(lendingBlocks.getBlockAmount())
                        .build();
                transactionService.unBlockAmount(unBlockTransactionDTO);
            }

            Account settlementAccountDetails = accountRepo.findById(settlementAccountID).get();
            BigDecimal settlementAccountAvailableBalance = settlementAccountDetails.getClearedBalance().
                    subtract(settlementAccountDetails.getBlockedBalance());

            // Persist Missed Repayments to LOAN REPAYMENTS till current date
            loanService.persistMissedRepayments(loanDetails);

            BigDecimal totalArrearsAmount = loanService.fetchTotalUnpaidAmount(loanDetails);
            BigDecimal repaymentAmount = totalArrearsAmount;
            blockAmount = totalArrearsAmount;
            LoanSchedule loanSchedule = fetchNextDue(loanDetails);

            // Adding next due amount to the arrears amount only if it is a repayment day
            if (isLookAheadDaysReached(loanSchedule, lookAheadDays, businessDate)) {
                blockAmount = blockAmount.add(loanSchedule.getRepaymentDue());
            }

            // Invoking Repayment Service
            if (repaymentAmount.compareTo(settlementAccountAvailableBalance) > 0) {
                repaymentAmount = settlementAccountAvailableBalance;
            }

            if (repaymentAmount.compareTo(BigDecimal.ZERO) > 0)
                loanService.processLoanRepayment(loanDetails, repaymentAmount);


            // Adding a new Look Ahead Day Block
            blockAmount = blockAmount.subtract(repaymentAmount);
            if (blockAmount.compareTo(BigDecimal.ZERO) > 0) {
                BlockTransactionDTO blockTransactionDTO = BlockTransactionDTO.builder()
                        .accountID(settlementAccountID)
                        .blockAmount(blockAmount)
                        .build();
                transactionService.blockAmount(blockTransactionDTO);
            }
            if (lendingBlocksOptional.isEmpty())
                createLendingBlocks(loanDetails, blockAmount);
            else {
                // Update Lending Blocks
                LendingBlocks lendingBlocks = lendingBlocksOptional.get();
                lendingBlocks.setBlockAmount(blockAmount);
                lendingBlocksRepo.save(lendingBlocks);
            }

            // Update Loan Status
            if (loanService.fetchTotalUnpaidAmount(loanDetails).compareTo(BigDecimal.ZERO) == 0 &&
                    loanService.fetchOutstandingAmount(loanDetails).compareTo(BigDecimal.ZERO) == 0) {
                loanService.updateLoanStatus(loanAccountID, LoanStatus.COMPLETED);
            }

        }
    }


    private LoanSchedule fetchNextDue(LoanDetails loanDetails){
        long repaymentNumber = loanService.fetchNextRepaymentNumber(loanDetails);
        LoanSchedule loanSchedule = loanScheduleRepo.findByLoanDetailsAndRepaymentNumber(loanDetails, repaymentNumber);
        return loanSchedule;
    }

    private void createLendingBlocks(LoanDetails loanDetails, BigDecimal blockAmount){
        LendingBlocks lendingBlocks = LendingBlocks.builder()
                .loanAccountID(loanDetails.getLoanAccountID())
                .settlementAccountID(loanDetails.getSettlementAccount().getAccountID())
                .blockAmount(blockAmount)
                .build();
        lendingBlocksRepo.save(lendingBlocks);
    }

    private Boolean isLookAheadDaysReached(LoanSchedule nextLoanSchedule, long lookAheadDays, Date businessDate){
        if(nextLoanSchedule == null)
            return Boolean.FALSE;
        Date lookAheadBlockingDate = Date.valueOf(nextLoanSchedule.getRepaymentDate().toLocalDate().minusDays(lookAheadDays));
        if(businessDate.toLocalDate().isEqual(lookAheadBlockingDate.toLocalDate()) ||
                businessDate.toLocalDate().isAfter(lookAheadBlockingDate.toLocalDate()))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }
}

