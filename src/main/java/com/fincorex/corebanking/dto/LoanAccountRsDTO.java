package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanAccountRsDTO {
    private String loanAccountID;
    private Long lookAheadDays;
    private String loanStatus;
    private DelinquencyProfileRsDTO delinquencyProfile;
    private BigDecimal loanAmount;
    private BigDecimal fixedInterestAmount;
    private Long loanTerm;
    private String settlementAccountID;
    private String disbursementAccountID;
    private Date maturityDate;
    private String collectionOrderProfile;
    private Character collectionOrderType;
    private DelinquencyStageRsDTO delinquencyStage;
    private List<LoanScheduleRsDTO> loanSchedule;
    private List<LoanRepaymentsRsDTO> loanRepayments;
}
