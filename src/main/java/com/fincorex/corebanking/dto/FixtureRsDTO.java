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
public class FixtureRsDTO {
    private String accountID;
    private BigDecimal fixtureAmount;
    private Long totalTerm;
    private String fixtureStatus;
    private BigDecimal totalAmountAtMaturity;
    private Date fixtureStartDate;
    private Date maturityDate;
}
