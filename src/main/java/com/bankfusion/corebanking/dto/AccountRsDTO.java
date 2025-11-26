package com.bankfusion.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountRsDTO {
    private String accountID;
    private CustomerRsDTO customer;
    private BranchRsDTO branch;
    private ProductInheritanceRsDTO subProduct;
    private BigDecimal clearedBalance;
    private BigDecimal blockedBalance;
    private BigDecimal debitAccruedInterest;
    private BigDecimal creditAccruedInterest;
    private BigDecimal debitInterestRate;
    private BigDecimal creditInterestRate;
    private Date openDate;
    private Date lastAccrualDate;
    private Boolean isClosed;
    private Date closureDate;
    private String closureReason;
}
