package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.BranchRqDTO;
import com.bankfusion.corebanking.dto.BranchRsDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.BranchNotFoundException;

import java.util.List;

public interface BranchService {
    public BranchRsDTO createBranch(BranchRqDTO branchRqDTO) throws BadRequestException;
    public BranchRsDTO fetchBranchDetails(String branchCode) throws BranchNotFoundException;
    public List<BranchRsDTO> fetchAllBranchDetails();
}
