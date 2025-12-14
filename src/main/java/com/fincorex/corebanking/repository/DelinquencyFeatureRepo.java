package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.DelinquencyFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelinquencyFeatureRepo extends JpaRepository<DelinquencyFeature, Long> {
}
