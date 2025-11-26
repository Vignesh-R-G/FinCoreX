package com.bankfusion.corebanking.repository;

import com.bankfusion.corebanking.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepo extends JpaRepository<Branch,String> {

    Branch findByIsHeadOffice(Boolean isHeadOffice);
}
