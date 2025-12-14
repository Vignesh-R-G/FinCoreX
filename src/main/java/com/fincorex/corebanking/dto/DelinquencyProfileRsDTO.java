package com.fincorex.corebanking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelinquencyProfileRsDTO {
    private Long delinquencyProfileID;
    private String profileDescription;
    private List<DelinquencyStageRsDTO> delinquencyStages;
}
