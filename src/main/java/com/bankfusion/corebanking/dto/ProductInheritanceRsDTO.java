package com.bankfusion.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInheritanceRsDTO {
    private String productContextCode;
    private BigDecimal debitInterestRate;
    private BigDecimal creditInterestRate;
    private String isoCurrencyCode;
    private String pnlAccount;
    private String glAccount;
    private String interestMethod;
    private ProductRsDTO product;
}
