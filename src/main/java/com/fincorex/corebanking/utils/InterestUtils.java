package com.fincorex.corebanking.utils;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.entity.Product;
import com.fincorex.corebanking.entity.ProductInheritance;
import com.fincorex.corebanking.enums.InterestMethod;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.events.InterestHistoryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class InterestUtils {
    public static BigDecimal calculateAccruedInterest(BigDecimal clearedBalance, BigDecimal interestRate, BigDecimal previousAccruedInterest, Date lastAccrualDate, Date currentDate){
        return previousAccruedInterest.add(
                (clearedBalance.multiply(interestRate).multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(lastAccrualDate.toLocalDate(), currentDate.toLocalDate()))))
                        .divide(BigDecimal.valueOf(ApiConstants.INTEREST_BASE_DAYS * 100), 2, RoundingMode.HALF_UP)
        ).setScale(2, RoundingMode.HALF_UP);
    }

    public static InterestHistoryEvent prepareInterestHistoryEvent(Object source, String eventType, String accountID, BigDecimal transactionAmount, BigDecimal debitInterestApplied, BigDecimal creditInterestApplied,
                                                                   BigDecimal debitInterestRateChange, BigDecimal creditInterestRateChange, Boolean isDebitInterestRateChangeMade, Boolean isCreditInterestRateChangeMade, Date transactionDate){
        InterestHistoryEvent interestHistoryEvent = new InterestHistoryEvent(source);
        interestHistoryEvent.setEventType(eventType);
        interestHistoryEvent.setAccountID(accountID);
        interestHistoryEvent.setTransactionAmount(transactionAmount);
        interestHistoryEvent.setDebitInterestApplied(debitInterestApplied);
        interestHistoryEvent.setCreditInterestApplied(creditInterestApplied);
        interestHistoryEvent.setDebitInterestRateChange(debitInterestRateChange);
        interestHistoryEvent.setCreditInterestRateChange(creditInterestRateChange);
        interestHistoryEvent.setIsDebitInterestRateChangeMade(isDebitInterestRateChangeMade);
        interestHistoryEvent.setIsCreditInterestRateChangeMade(isCreditInterestRateChangeMade);
        interestHistoryEvent.setDate(transactionDate);
        return interestHistoryEvent;
    }

    public static Boolean isInterestAccrualProduct(ProductInheritance productInheritance) {
        Product product = productInheritance.getProduct();
        if(product.getProductType().equals(ProductType.INTERNAL.name()))
            return Boolean.FALSE;
        if(productInheritance.getInterestMethod().equals(InterestMethod.FIXED.name()))
            return Boolean.FALSE;
        return Boolean.TRUE;
    }
}
