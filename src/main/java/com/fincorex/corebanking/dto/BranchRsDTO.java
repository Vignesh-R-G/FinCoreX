package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchRsDTO {
    private String branchCode;
    private String branchName;
    private Boolean isHeadOffice;
}
