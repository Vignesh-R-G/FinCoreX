package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAccountRqDTO {
    @NotNull(message = "Customer ID Field is Mandatory")
    private String customerID;

    @NotBlank(message = "Branch Code is Mandatory")
    private String branchCode;

    @NotBlank(message = "Sub-Product ID is Mandatory")
    private String subProductID;
}
