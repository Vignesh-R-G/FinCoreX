package com.fincorex.corebanking.repository;

import com.fincorex.corebanking.entity.DelinquencyMarkingTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelinquencyMarkingTagRepo extends JpaRepository<DelinquencyMarkingTag, Long> {
}
