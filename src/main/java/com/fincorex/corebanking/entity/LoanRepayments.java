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

@Table(name = "LOAN_REPAYMENTS",schema = "BANKFUSION")
public class LoanRepayments {
    @Id
    private String loanRepaymentID;
    private Long repaymentNumber;
    private Date repaymentDate;
    private BigDecimal totalRepaymentDue;
    private BigDecimal totalRepaymentPaid;
    private BigDecimal totalRepaymentOverDue;
    private BigDecimal principalDue;
    private BigDecimal principalPaid;
    private BigDecimal principalOverDue;
    private BigDecimal interestDue;
    private BigDecimal interestPaid;
    private BigDecimal interestOverDue;

    @ManyToOne
    @JoinColumn(
            name = "loan_account_id",
            referencedColumnName = "loanAccountID"
    )
    private LoanDetails loanDetails;

    @Version
    private Long version;
}
