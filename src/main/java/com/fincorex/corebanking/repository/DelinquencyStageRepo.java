package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.DelinquencyStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelinquencyStageRepo extends JpaRepository<DelinquencyStage, Long> {
}
