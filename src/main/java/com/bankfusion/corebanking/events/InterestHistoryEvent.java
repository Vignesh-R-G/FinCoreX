package com.bankfusion.corebanking.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
public class InterestHistoryEvent extends ApplicationEvent {
    private String eventType;
    private String accountID;
    private Date date;
    private BigDecimal transactionAmount;
    private BigDecimal debitInterestApplied;
    private BigDecimal creditInterestApplied;
    private BigDecimal debitInterestRateChange;
    private BigDecimal creditInterestRateChange;
    private Boolean isDebitInterestRateChangeMade;
    private Boolean isCreditInterestRateChangeMade;

    public InterestHistoryEvent(Object source) {
        super(source);
    }

}
