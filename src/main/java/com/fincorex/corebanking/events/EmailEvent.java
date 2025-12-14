package com.fincorex.corebanking.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
public class EmailEvent extends ApplicationEvent {
    private String accountID;
    private String customerEmail;
    private String customerName;
    private Date date;
    private BigDecimal transactionAmount;
    private BigDecimal availableBalance;
    private String eventType;

    public EmailEvent(Object source) {
        super(source);
    }
}
