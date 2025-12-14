package com.fincorex.corebanking.utils;

import com.fincorex.corebanking.events.EmailEvent;

import java.math.BigDecimal;
import java.sql.Date;

public class EmailUtil {
    public static EmailEvent prepareEmailEvent(Object source, String eventType, String accountID, String customerEmail,
                                               String customerName, Date date, BigDecimal transactionAmount, BigDecimal availableBalance){
        EmailEvent emailEvent = new EmailEvent(source);
        emailEvent.setEventType(eventType);
        emailEvent.setAccountID(accountID);
        emailEvent.setCustomerEmail(customerEmail);
        emailEvent.setCustomerName(customerName);
        emailEvent.setDate(date);
        emailEvent.setTransactionAmount(transactionAmount);
        emailEvent.setAvailableBalance(availableBalance);
        return emailEvent;
    }
}
