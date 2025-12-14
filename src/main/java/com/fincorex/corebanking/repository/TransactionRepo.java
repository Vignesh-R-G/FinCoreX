package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<TransactionDetails, Long> {
    List<TransactionDetails> findAllByTransactionID(String transactionID);
}
