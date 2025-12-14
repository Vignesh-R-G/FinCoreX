package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelinquencyStageRsDTO {
    private Long stageID;
    private String stageDescription;
    private Long overDueDays;
}
