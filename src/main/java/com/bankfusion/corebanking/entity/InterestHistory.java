package com.bankfusion.corebanking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "INTEREST_HISTORY", schema = "BANKFUSION")

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterestHistory {
    @Id
    private String interestHistoryID;
    private String accountID;
    private Date date;
    private BigDecimal openingBalance;
    private BigDecimal transactionAmount;
    private BigDecimal closingBalance;
    private BigDecimal openingDebitInterest;
    private BigDecimal debitInterestApplied;
    private BigDecimal closingDebitInterest;
    private BigDecimal openingCreditInterest;
    private BigDecimal creditInterestApplied;
    private BigDecimal closingCreditInterest;
    private BigDecimal openingDebitInterestRate;
    private BigDecimal closingDebitInterestRate;
    private BigDecimal openingCreditInterestRate;
    private BigDecimal closingCreditInterestRate;
}
