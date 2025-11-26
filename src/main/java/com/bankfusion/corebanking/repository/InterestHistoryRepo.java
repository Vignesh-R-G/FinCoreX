package com.bankfusion.corebanking.repository;

import com.bankfusion.corebanking.entity.InterestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestHistoryRepo extends JpaRepository<InterestHistory,String> {
    List<InterestHistory> findAllByAccountIDOrderByDateDesc(String accountID);
}
