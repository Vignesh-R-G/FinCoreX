package com.bankfusion.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "ACCOUNT", schema = "BANKFUSION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    private String accountID;
    @ManyToOne(
        cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "customer_id",
            referencedColumnName = "customerID"
    )
    private Customer customer;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "branch_code",
            referencedColumnName = "branchCode"
    )
    private Branch branch;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "sub_product_id",
            referencedColumnName = "productContextCode"
    )
    private ProductInheritance subProduct;

    private BigDecimal clearedBalance;

    private BigDecimal blockedBalance;

    private BigDecimal debitAccruedInterest;

    private BigDecimal creditAccruedInterest;

    private BigDecimal debitInterestRate;

    private BigDecimal creditInterestRate;

    private Date openDate;

    private Date lastAccrualDate;

    private Boolean isClosed;

    private Date closureDate;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String closureReason;

    @Version
    private Long version;
}
