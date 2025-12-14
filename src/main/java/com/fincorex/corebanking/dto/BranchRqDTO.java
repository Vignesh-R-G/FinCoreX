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
public class BranchRqDTO {
    @NotBlank(message = "Branch Code is Mandatory")
    private String branchCode;

    @NotBlank(message = "Branch Name is Mandatory")
    private String branchName;

    @NotNull(message = "Is Head Office is Mandatory")
    private Boolean isHeadOffice;
}
