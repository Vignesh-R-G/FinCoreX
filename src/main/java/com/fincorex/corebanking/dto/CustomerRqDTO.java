package com.fincorex.corebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRqDTO {
    private String customerID;

    @NotBlank(message = "Customer Name is Mandatory")
    private String customerName;

    @NotNull(message = "Customer DOB is Mandatory")
    @Past(message = "DOB should be the Past Date")
    private Date customerDOB;

    @NotBlank(message = "Customer Address is Mandatory")
    private String customerAddress;

    @NotBlank(message = "Customer Mobile Number is Mandatory")
    private String mobileNumber;

    @NotNull(message = "Customer Gender is Mandatory")
    private Character gender;

    @NotBlank(message = "Customer Email is Mandatory")
    private String customerEmail;
}
