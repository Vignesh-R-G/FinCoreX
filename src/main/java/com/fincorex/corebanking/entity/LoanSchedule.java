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
public class LoanSchedule {
    @Id
    private String loanScheduleID;
    private Long repaymentNumber;
    private BigDecimal repaymentDue;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private Date repaymentDate;

    @ManyToOne
    @JoinColumn(
            name = "loan_account_id",
            referencedColumnName = "loanAccountID"
    )
    private LoanDetails loanDetails;

    @Version
    private Long version;
}
