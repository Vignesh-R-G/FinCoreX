package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.InterestAccrualTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestAccrualTagRepo extends JpaRepository<InterestAccrualTag, String> {
}
