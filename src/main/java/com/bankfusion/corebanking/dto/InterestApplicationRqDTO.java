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
public class InterestApplicationRqDTO {
    @NotBlank(message = "Account ID is Mandatory")
    private String accountID;

    @NotBlank(message = "Interest Application Type is Mandatory")
    private Character interestApplicationType;
}
