package com.fincorex.corebanking.batch.interestaccrual;

import com.fincorex.corebanking.dto.InterestEnquiryRsDTO;
import com.fincorex.corebanking.dto.TransactionDetailsRqDTO;
import com.fincorex.corebanking.dto.TransactionRqDTO;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.InterestAccrualTag;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.InterestAccrualTagRepo;
import com.fincorex.corebanking.service.AccountService;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.InterestUtils;
import com.fincorex.corebanking.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class InterestAccrualItemProcessor implements ItemProcessor<InterestAccrualTag, InterestAccrualTag> {

    @Autowired
    private InterestAccrualTagRepo interestAccrualTagRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private AccountRepo accountRepo;

    private static final Logger logger = LoggerFactory.getLogger(InterestAccrualItemProcessor.class);


    @Override
    public InterestAccrualTag process(InterestAccrualTag interestAccrualRecord) throws InterruptedException {
        try {
            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();
            logger.info("Interest Accrual for Account ID: {}, Thread: {} , ThreadID : {}", interestAccrualRecord.getAccountID(), threadName, threadID);
            String accountID = interestAccrualRecord.getAccountID();

            Account account = accountRepo.findById(accountID).get();

            if(!InterestUtils.isInterestAccrualProduct(account.getSubProduct()))
                return interestAccrualRecord;

            InterestEnquiryRsDTO interestEnquiryRsDTO = accountService.getInterestDetails(accountID);
            BigDecimal creditAccruedInterest = interestEnquiryRsDTO.getCreditAccruedInterest();
            BigDecimal debitAccruedInterest = interestEnquiryRsDTO.getDebitAccruedInterest();

            if(creditAccruedInterest.compareTo(BigDecimal.ZERO) > 0){
                // Reversal Transaction
                if(account.getLastAccrualAmount() != null && account.getLastAccrualAmount().compareTo(BigDecimal.ZERO) > 0) {
                    List<TransactionDetailsRqDTO> reversalTransactionDetailsRqDTOList = new ArrayList<>();
                    reversalTransactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getGlAccount(), account.getLastAccrualAmount().negate(), 'D'));
                    reversalTransactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getPnlAccount(), account.getLastAccrualAmount(), 'C'));

                    TransactionRqDTO reversalTransactionRqDTO = TransactionRqDTO.builder()
                            .transactionDetails(reversalTransactionDetailsRqDTOList)
                            .build();
                    transactionService.processTransaction(reversalTransactionRqDTO, TransactionCode.IA1);
                }
                List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
                transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getPnlAccount(), creditAccruedInterest.negate(), 'D'));
                transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getGlAccount(), creditAccruedInterest, 'C'));

                TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                        .transactionDetails(transactionDetailsRqDTOList)
                        .build();
                transactionService.processTransaction(transactionRqDTO, TransactionCode.IA0);

                updateLastAccrualDetails(accountID, creditAccruedInterest);

            }
            if(debitAccruedInterest.compareTo(BigDecimal.ZERO) > 0){
                if(account.getLastAccrualAmount() != null && account.getLastAccrualAmount().compareTo(BigDecimal.ZERO) > 0) {
                    List<TransactionDetailsRqDTO> reversalTransactionDetailsRqDTOList = new ArrayList<>();
                    reversalTransactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getPnlAccount(), account.getLastAccrualAmount().negate(), 'D'));
                    reversalTransactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getGlAccount(), account.getLastAccrualAmount(), 'C'));

                    TransactionRqDTO reversalTransactionRqDTO = TransactionRqDTO.builder()
                            .transactionDetails(reversalTransactionDetailsRqDTOList)
                            .build();
                    transactionService.processTransaction(reversalTransactionRqDTO, TransactionCode.IA1);
                }

                List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
                transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getGlAccount(), debitAccruedInterest.negate(), 'D'));
                transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getSubProduct().getPnlAccount(), debitAccruedInterest, 'C'));

                TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                        .transactionDetails(transactionDetailsRqDTOList)
                        .build();
                transactionService.processTransaction(transactionRqDTO, TransactionCode.IA0);

                updateLastAccrualDetails(accountID, debitAccruedInterest);
            }


        } catch (Exception exception){
            logger.error("Error occurred on the Interest Accrual Processing for the Account ID : {}" ,interestAccrualRecord.getAccountID());
            return null;
        }

        return interestAccrualRecord;
    }


    public void updateLastAccrualDetails(String accountID, BigDecimal lastAccrualAmount) {
        Account account = accountRepo.findById(accountID).get();
        account.setLastAccrualAmount(lastAccrualAmount);
        accountRepo.save(account);
    }

}
