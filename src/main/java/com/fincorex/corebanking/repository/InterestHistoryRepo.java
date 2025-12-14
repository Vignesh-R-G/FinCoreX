package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.InterestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestHistoryRepo extends JpaRepository<InterestHistory,String> {
    List<InterestHistory> findAllByAccountIDOrderByDateDesc(String accountID);
}
