package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEstablishmentRqDTO {
    @NotNull(message = "Account Opening Details are Mandatory")
    private OpenAccountRqDTO openAccountRqDTO;

    @NotNull(message = "Look Ahead Days is Mandatory")
    @PositiveOrZero(message = "Look Ahead Days should be positive or zero")
    private Long lookAheadDays;

    @NotNull(message = "Delinquency Profile ID is Mandatory")
    private Long delinquencyProfileID;

    @NotNull(message = "Loan Amount is Mandatory")
    @Negative(message = "Loan Amount should be negative")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan Term is Mandatory")
    @Positive(message = "Loan Term should be positive.It defines the loan term in months")
    private Long loanTermInMonths;

    @NotNull(message = "Fixed Interest Amount is Mandatory")
    @PositiveOrZero(message = "Fixed Interest Amount should be Positive or zero")
    private BigDecimal fixedInterestAmount;

    @NotBlank(message = "Settlement Account is Mandatory")
    private String settlementAccount;

    @NotBlank(message = "Disbursement Account is Mandatory")
    private String disbursementAccount;

    @NotBlank(message = "Collection Order Profile is Mandatory")
    private String collectionOrderProfile;

    @NotNull(message = "Collection Order Type is Mandatory")
    private Character collectionOrderType;

    private List<LoanScheduleRqDTO> loanScheduleRqDTOList;
}
