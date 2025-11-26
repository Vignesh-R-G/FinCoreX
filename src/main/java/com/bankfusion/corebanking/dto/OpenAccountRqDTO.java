package com.bankfusion.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAccountRqDTO {
    @NotBlank(message = "Account ID is Mandatory")
    private String accountID;

    @NotBlank(message = "Customer ID is Mandatory")
    private String customerID;

    @NotBlank(message = "Branch Code is Mandatory")
    private String branchCode;

    @NotBlank(message = "Sub-Product ID is Mandatory")
    private String subProductID;
}
