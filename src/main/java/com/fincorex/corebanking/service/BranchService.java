package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.BranchRqDTO;
import com.fincorex.corebanking.dto.BranchRsDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.BranchNotFoundException;

import java.util.List;

public interface BranchService {
    public BranchRsDTO createBranch(BranchRqDTO branchRqDTO) throws BadRequestException;
    public BranchRsDTO fetchBranchDetails(String branchCode) throws BranchNotFoundException;
    public List<BranchRsDTO> fetchAllBranchDetails();
}
