package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessDateDTO {
    @NotNull(message = "Business Date is Mandatory")
    private Date businessDate;
}
