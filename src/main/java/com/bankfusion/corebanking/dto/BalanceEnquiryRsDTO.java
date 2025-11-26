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
public class BalanceEnquiryRsDTO {
    private BigDecimal clearedBalance;
    private BigDecimal blockedBalance;
    private BigDecimal availableBalance;
}
