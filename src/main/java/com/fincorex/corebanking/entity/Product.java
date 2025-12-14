package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCT", schema = "BANKFUSION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    private String productID;
    private String productName;
    private String productType;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<ProductInheritance> productInheritanceList = new ArrayList<>();

    @Version
    private Long version;
}
