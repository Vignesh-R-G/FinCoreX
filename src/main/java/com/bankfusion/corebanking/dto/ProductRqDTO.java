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
public class ProductRqDTO {
    @NotBlank(message = "Product ID is Mandatory")
    private String productID;

    @NotBlank(message = "Product Name is Mandatory")
    private String productName;

    @NotBlank(message = "Product Type is Mandatory")
    private String productType;
}
