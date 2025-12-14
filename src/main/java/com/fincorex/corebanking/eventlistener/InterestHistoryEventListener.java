package com.fincorex.corebanking.eventlistener;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.InterestHistory;
import com.fincorex.corebanking.events.InterestHistoryEvent;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.InterestHistoryRepo;
import com.fincorex.corebanking.utils.InterestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class InterestHistoryEventListener {

    @Autowired
    private InterestHistoryRepo interestHistoryRepo;

    @Autowired
    private AccountRepo accountRepo;

    private final ConcurrentHashMap<String, Object> accountLocks = new ConcurrentHashMap<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistInterestHistory(InterestHistoryEvent interestHistoryEvent) {
        Object lock = null;
        if(!accountLocks.containsKey(interestHistoryEvent.getAccountID()))
            accountLocks.put(interestHistoryEvent.getAccountID(),new Object());
        lock = accountLocks.get(interestHistoryEvent.getAccountID());

        synchronized (lock) {
            String eventType = interestHistoryEvent.getEventType();
            if (eventType.equals(ApiConstants.INT_HISTORY_INITIALIZE_EVENT)) {
                Account account = accountRepo.findById(interestHistoryEvent.getAccountID()).get();
                initializeInterestHistory(interestHistoryEvent, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, account.getDebitInterestRate(), account.getCreditInterestRate());
            } else if (eventType.equals(ApiConstants.INT_HISTORY_TRANSACTION_EVENT) || eventType.equals(ApiConstants.INT_HISTORY_RATE_CHANGE_EVENT)) {
                InterestHistory interestHistory = fetchInterestHistoryOnSpecifiedDate(interestHistoryEvent.getAccountID(), interestHistoryEvent.getDate());
                if (interestHistory == null) {
                    InterestHistory previousInterestHistory = fetchPreviousInterestHistory(interestHistoryEvent.getAccountID());
                    BigDecimal openingCreditAccruedInterest = InterestUtils.calculateAccruedInterest(previousInterestHistory.getClosingBalance(),
                            previousInterestHistory.getClosingCreditInterestRate(), previousInterestHistory.getClosingCreditInterest(),
                            previousInterestHistory.getDate(), interestHistoryEvent.getDate());

                    BigDecimal openingDebitAccruedInterest = InterestUtils.calculateAccruedInterest(previousInterestHistory.getClosingBalance(),
                            previousInterestHistory.getClosingDebitInterestRate(), previousInterestHistory.getClosingDebitInterest(),
                            previousInterestHistory.getDate(), interestHistoryEvent.getDate());

                    initializeInterestHistory(interestHistoryEvent, previousInterestHistory.getClosingBalance(), openingDebitAccruedInterest,
                            openingCreditAccruedInterest, previousInterestHistory.getClosingDebitInterestRate(),
                            previousInterestHistory.getClosingCreditInterestRate());
                } else {
                    buildInterestHistoryBasedOnTransEvent(interestHistory, interestHistoryEvent);
                }
            }
        }
    }

    private InterestHistory fetchInterestHistoryOnSpecifiedDate(String accountID, Date transactionDate){
        String interestHistoryID = accountID + "_" + transactionDate;
        Optional<InterestHistory> interestHistory = interestHistoryRepo.findById(interestHistoryID);
        return interestHistory.orElse(null);
    }

    private InterestHistory fetchPreviousInterestHistory(String accountID){
        List<InterestHistory> interestHistoryList = interestHistoryRepo.findAllByAccountIDOrderByDateDesc(accountID);
        return interestHistoryList.get(0);
    }

    private void initializeInterestHistory(InterestHistoryEvent interestHistoryEvent, BigDecimal openingBalance, BigDecimal openingDebitInterest,
                                           BigDecimal openingCreditInterest, BigDecimal openingDebitInterestRate,
                                           BigDecimal openingCreditInterestRate) {

        String interestHistoryID = interestHistoryEvent.getAccountID() + "_" + interestHistoryEvent.getDate();
        InterestHistory interestHistory = InterestHistory.builder()
                .interestHistoryID(interestHistoryID)
                .accountID(interestHistoryEvent.getAccountID())
                .date(interestHistoryEvent.getDate())
                .openingBalance(openingBalance)
                .transactionAmount(BigDecimal.ZERO)
                .closingBalance(openingBalance)
                .openingDebitInterest(openingDebitInterest)
                .debitInterestApplied(BigDecimal.ZERO)
                .closingDebitInterest(openingDebitInterest)
                .openingCreditInterest(openingCreditInterest)
                .creditInterestApplied(BigDecimal.ZERO)
                .closingCreditInterest(openingCreditInterest)
                .openingDebitInterestRate(openingDebitInterestRate)
                .closingDebitInterestRate(openingDebitInterestRate)
                .openingCreditInterestRate(openingCreditInterestRate)
                .closingCreditInterestRate(openingCreditInterestRate)
                .build();
        buildInterestHistoryBasedOnTransEvent(interestHistory, interestHistoryEvent);
    }

    private void buildInterestHistoryBasedOnTransEvent(InterestHistory interestHistory, InterestHistoryEvent interestHistoryEvent) {
        Account account = accountRepo.findById(interestHistory.getAccountID()).get();

        interestHistory.setTransactionAmount(interestHistory.getTransactionAmount()
                .add(interestHistoryEvent.getTransactionAmount()));
        interestHistory.setClosingBalance(interestHistory.getClosingBalance()
                .add(interestHistoryEvent.getTransactionAmount()));
        interestHistory.setDebitInterestApplied(interestHistory.getDebitInterestApplied()
                .add(interestHistoryEvent.getDebitInterestApplied()));
        interestHistory.setClosingDebitInterest(interestHistory.getClosingDebitInterest()
                .subtract(interestHistoryEvent.getDebitInterestApplied()));
        interestHistory.setCreditInterestApplied(interestHistory.getCreditInterestApplied()
                .add(interestHistoryEvent.getCreditInterestApplied()));
        interestHistory.setClosingCreditInterest(interestHistory.getClosingCreditInterest()
                .subtract(interestHistoryEvent.getCreditInterestApplied()));

        if(interestHistoryEvent.getIsDebitInterestRateChangeMade()) {
            interestHistory.setClosingDebitInterestRate(interestHistoryEvent.getDebitInterestRateChange());
            account.setDebitInterestRate(interestHistoryEvent.getDebitInterestRateChange());
        }
        if(interestHistoryEvent.getIsCreditInterestRateChangeMade()) {
            interestHistory.setClosingCreditInterestRate(interestHistoryEvent.getCreditInterestRateChange());
            account.setCreditInterestRate(interestHistoryEvent.getCreditInterestRateChange());
        }

        interestHistoryRepo.save(interestHistory);

        account.setDebitAccruedInterest(interestHistory.getClosingDebitInterest());
        account.setCreditAccruedInterest(interestHistory.getClosingCreditInterest());
        account.setLastAccrualDate(interestHistory.getDate());
        accountRepo.save(account);
    }
}
