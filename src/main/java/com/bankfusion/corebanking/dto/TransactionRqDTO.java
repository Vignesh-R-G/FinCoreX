package com.bankfusion.corebanking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRqDTO {
    @NotNull(message = "Transaction Details is Mandatory")
    private List<TransactionDetailsRqDTO> transactionDetails;
}
