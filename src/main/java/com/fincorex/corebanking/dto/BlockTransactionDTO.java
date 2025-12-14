package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockTransactionDTO {
    @NotBlank(message = "Account ID is Mandatory")
    private String accountID;

    @NotNull(message = "Block Amount is Mandatory")
    @Positive(message = "Block Amount should be Positive and non zero")
    private BigDecimal blockAmount;
}
