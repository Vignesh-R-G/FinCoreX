package com.bankfusion.corebanking.dto;

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
public class ProductInheritanceRqDTO {
    @NotBlank(message = "Product Context Code is Mandatory")
    private String productContextCode;

    @NotNull(message = "Debit Interest Rate is Mandatory")
    @PositiveOrZero
    private BigDecimal debitInterestRate;

    @NotNull(message = "Credit Interest Rate is Mandatory")
    @PositiveOrZero
    private BigDecimal creditInterestRate;

    @NotBlank(message = "Interest Method is Mandatory")
    private String interestMethod;

    @NotBlank(message = "Iso Currency Code is Mandatory")
    private String isoCurrencyCode;

    private String pnlAccount;
    private String glAccount;

    @NotBlank(message = "Product ID is Mandatory")
    private String productID;
}
