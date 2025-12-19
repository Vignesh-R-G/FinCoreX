package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.FixedDepositDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixtureRepo extends JpaRepository<FixedDepositDetails, String> {
    List<FixedDepositDetails> findAllByFixtureStatus(String name);

    List<FixedDepositDetails> findAllByFundingAccount(Account account);

    List<FixedDepositDetails> findAllByPayAwayAccount(Account account);
}
