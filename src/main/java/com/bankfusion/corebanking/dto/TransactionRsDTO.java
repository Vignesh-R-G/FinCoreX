package com.bankfusion.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRsDTO {
    private List<TransactionDetailsRsDTO> transactionDetails;
    private String transactionID;
    private String transactionCode;
    private String narration;
}
