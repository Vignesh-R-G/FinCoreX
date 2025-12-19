package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "FIXED_DEPOSIT_DETAILS")
public class FixedDepositDetails {
    @Id
    private String fixtureAccountID;

    @OneToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "account_id",
            referencedColumnName = "accountID"
    )
    private Account account;

    private Date fixtureStartDate;

    private Date maturityDate;

    private Long term;

    private String fixtureStatus;

    private BigDecimal fixtureAmount;

    @ManyToOne
    @JoinColumn(
            name = "funding_account",
            referencedColumnName = "accountID"
    )
    private Account fundingAccount;

    @ManyToOne
    @JoinColumn(
            name = "payaway_account",
            referencedColumnName = "accountID"
    )
    private Account payAwayAccount;
}
