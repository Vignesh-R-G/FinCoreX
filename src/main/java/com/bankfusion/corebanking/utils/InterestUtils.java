package com.bankfusion.corebanking.utils;

import com.bankfusion.corebanking.constants.ApiConstants;
import com.bankfusion.corebanking.entity.Product;
import com.bankfusion.corebanking.entity.ProductInheritance;
import com.bankfusion.corebanking.enums.InterestMethod;
import com.bankfusion.corebanking.enums.ProductType;
import com.bankfusion.corebanking.events.InterestHistoryEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Period;

public class InterestUtils {
    public static BigDecimal calculateAccruedInterest(BigDecimal clearedBalance, BigDecimal interestRate, BigDecimal previousAccruedInterest, Date lastAccrualDate, Date currentDate){
        return previousAccruedInterest.add(
                (clearedBalance.multiply(interestRate).multiply(BigDecimal.valueOf(Period.between(lastAccrualDate.toLocalDate(), currentDate.toLocalDate()).getDays())))
                        .divide(BigDecimal.valueOf(ApiConstants.INTEREST_BASE_DAYS * 100), RoundingMode.HALF_UP)
        );
    }

    public static InterestHistoryEvent prepareInterestHistoryEvent(Object source, String eventType, String accountID, BigDecimal transactionAmount, BigDecimal debitInterestApplied, BigDecimal creditInterestApplied, BigDecimal debitInterestRateChange, BigDecimal creditInterestRateChange, Boolean isDebitInterestRateChangeMade, Boolean isCreditInterestRateChangeMade){
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
