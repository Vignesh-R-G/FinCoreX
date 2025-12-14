package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetailsRqDTO {
    @NotBlank(message = "Account ID is Mandatory")
    private String accountID;

    @NotNull(message = "Amount is Mandatory")
    @PositiveOrZero(message = "Amount should be Positive or Zero")
    private BigDecimal amount;

    @NotNull(message = "Debit Credit Flag is Mandatory")
    private Character debitCreditFlag;

}
