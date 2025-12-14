package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.DelinquencyStage;
import com.fincorex.corebanking.entity.LoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDetailsRepo extends JpaRepository<LoanDetails, String> {
    LoanDetails findByDelinquencyStage(DelinquencyStage delinquencyStage);

    List<LoanDetails> findAllBySettlementAccount(Account account);
}
