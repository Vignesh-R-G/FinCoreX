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
public class TransactionDetailsRsDTO {
    private String accountID;

    private BigDecimal amount;

    private Character debitCreditFlag;

}
