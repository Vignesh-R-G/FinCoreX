package com.fincorex.corebanking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(name = "LOAN_DETAILS", schema = "BANKFUSION")
public class LoanDetails {
    @Id
    private String loanAccountID;
    private Long lookAheadDays;
    private String loanStatus;
    private BigDecimal loanAmount;
    private Long loanTermInMonths;
    private BigDecimal fixedInterestAmount;
    private Date loanStartDate;
    private Date loanMaturityDate;
    private String collectionOrderProfile;
    private Character collectionOrderType;

    @ManyToOne
    @JoinColumn(
            name = "settlement_account_id",
            referencedColumnName = "accountID"
    )
    private Account settlementAccount;

    @ManyToOne
    @JoinColumn(
            name = "disbursement_account_id",
            referencedColumnName = "accountID"
    )
    private Account disbursementAccount;

    @ManyToOne
    @JoinColumn(
            name = "delinquency_profile",
            referencedColumnName = "delinquencyProfileID"
    )
    private DelinquencyFeature delinquencyFeature;

    @ManyToOne
    @JoinColumn(
            name = "delinquency_stage",
            referencedColumnName = "stageID"
    )
    private DelinquencyStage delinquencyStage;

    @OneToOne
    @JoinColumn(
            name = "account_id",
            referencedColumnName = "accountID"
    )
    private Account account;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "loanDetails"
    )
    @Builder.Default
    private List<LoanRepayments> loanRepayments = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "loanDetails"
    )
    @Builder.Default
    private List<LoanSchedule> loanSchedules = new ArrayList<>();

    @Version
    private Long version;
}
