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
public class LoanScheduleRsDTO {
    private Long repaymentNumber;
    private BigDecimal repaymentDue;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private Date repaymentDate;
}
