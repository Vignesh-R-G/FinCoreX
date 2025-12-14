package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCT_INHERITANCE", schema = "BANKFUSION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInheritance {
    @Id
    private String productContextCode;
    private BigDecimal debitInterestRate;
    private BigDecimal creditInterestRate;
    private String isoCurrencyCode;
    private String pnlAccount;
    private String glAccount;
    private String interestMethod;

    @ManyToOne
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "productID"
    )
    private Product product;

    @OneToMany(
            mappedBy = "subProduct",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<Account> accountList = new ArrayList<>();

    @Version
    private Long version;
}
