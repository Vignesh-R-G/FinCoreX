package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelinquencyProfileRqDTO {
    @NotBlank(message = "Profile Description is Mandatory")
    private String profileDescription;

}
