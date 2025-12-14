package com.fincorex.corebanking.batch.interestapplication;

import com.fincorex.corebanking.dto.InterestApplicationRqDTO;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.InterestApplicationTag;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.InterestApplicationTagRepo;
import com.fincorex.corebanking.service.AccountService;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InterestApplicationItemProcessor implements ItemProcessor<InterestApplicationTag, InterestApplicationTag> {

    @Autowired
    private InterestApplicationTagRepo interestApplicationTagRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private AccountRepo accountRepo;

    private static final Logger logger = LoggerFactory.getLogger(InterestApplicationItemProcessor.class);


    @Override
    public InterestApplicationTag process(InterestApplicationTag interestApplicationRecord) throws InterruptedException {
        try {
            String threadName = Thread.currentThread().getName();
            long threadID = Thread.currentThread().getId();
            logger.info("Interest Application for Account ID: {}, Thread: {} , ThreadID : {}", interestApplicationRecord.getAccountID(), threadName, threadID);
            String accountID = interestApplicationRecord.getAccountID();

            Account account = accountRepo.findById(accountID).get();
            String productType = account.getSubProduct().getProduct().getProductType();

            // Credit Interest Application
            if(productType.equals(ProductType.SA.name()) || productType.equals(ProductType.CA.name())) {
                InterestApplicationRqDTO creditInterestApplicationRqDTO = InterestApplicationRqDTO.builder()
                        .accountID(accountID)
                        .interestApplicationType('C')
                        .build();
                transactionService.processInterestApplication(creditInterestApplicationRqDTO);
            }

            // Debit Interest Application
            if(productType.equals(ProductType.CA.name())) {
                InterestApplicationRqDTO debitInterestApplicationRqDTO = InterestApplicationRqDTO.builder()
                        .accountID(accountID)
                        .interestApplicationType('D')
                        .build();
                transactionService.processInterestApplication(debitInterestApplicationRqDTO);
            }

        } catch (Exception exception){
            logger.error("Error occurred on the Interest Application Processing for the Account ID : {}" ,interestApplicationRecord.getAccountID());
            return null;
        }

        return interestApplicationRecord;
    }

}
