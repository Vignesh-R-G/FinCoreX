package com.fincorex.corebanking.dto;

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
public class LoanRepaymentsRsDTO {
    private Long repaymentNumber;
    private Date repaymentDate;
    private BigDecimal totalRepaymentDue;
    private BigDecimal totalRepaymentPaid;
    private BigDecimal totalRepaymentOverDue;
    private BigDecimal principalDue;
    private BigDecimal principalPaid;
    private BigDecimal principalOverDue;
    private BigDecimal interestDue;
    private BigDecimal interestPaid;
    private BigDecimal interestOverDue;
}
