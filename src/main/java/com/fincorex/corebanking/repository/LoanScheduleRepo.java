package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.entity.LoanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanScheduleRepo extends JpaRepository<LoanSchedule,String> {
    LoanSchedule findByLoanDetailsAndRepaymentNumber(LoanDetails loanDetails, long repaymentNumber);
}
