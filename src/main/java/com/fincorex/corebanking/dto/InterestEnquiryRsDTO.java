package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterestEnquiryRsDTO {
    private String accountID;
    private BigDecimal creditInterestRate;
    private BigDecimal creditAccruedInterest;
    private BigDecimal debitInterestRate;
    private BigDecimal debitAccruedInterest;
}
