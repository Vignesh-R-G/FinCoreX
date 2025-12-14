package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.LendingBlocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LendingBlocksRepo extends JpaRepository<LendingBlocks, String> {
    List<LendingBlocks> findBySettlementAccountID(String accountID);
}
