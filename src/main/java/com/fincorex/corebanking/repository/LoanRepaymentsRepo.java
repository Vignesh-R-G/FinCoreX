package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.entity.LoanRepayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentsRepo extends JpaRepository<LoanRepayments, String> {
    long countByLoanDetails(LoanDetails loanDetails);

    List<LoanRepayments> findAllByLoanDetailsOrderByRepaymentNumberDesc(LoanDetails loanDetails);

    LoanRepayments findByLoanDetailsAndRepaymentNumber(LoanDetails loanDetails, Long repaymentNumber);

    List<LoanRepayments> findAllByLoanDetailsOrderByRepaymentNumberAsc(LoanDetails loanDetails);
}
