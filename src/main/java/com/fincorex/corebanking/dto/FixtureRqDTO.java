package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixtureRqDTO {
    @NotNull(message = "Account Opening Details is Mandatory")
    private OpenAccountRqDTO openAccountRqDTO;

    @NotNull(message = "Fixture Amount is Mandatory")
    @Positive(message = "Fixture Amount should be Positive")
    private BigDecimal fixtureAmount;

    @NotNull(message = "Fixture Term is Mandatory")
    @Positive(message = "Fixture Term should be Positive")
    private Long term;

    @NotBlank(message = "Funding Account is Mandatory")
    private String fundingAccount;

    @NotBlank(message = "Pay Away Account is Mandatory")
    private String payAwayAccount;
}
