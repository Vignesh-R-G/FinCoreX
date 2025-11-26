package com.bankfusion.corebanking.service.impl;

import com.bankfusion.corebanking.dto.BranchRqDTO;
import com.bankfusion.corebanking.dto.BranchRsDTO;
import com.bankfusion.corebanking.entity.Branch;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.BranchNotFoundException;
import com.bankfusion.corebanking.repository.BranchRepo;
import com.bankfusion.corebanking.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {
    @Autowired
    private BranchRepo branchRepo;

    @Override
    @Transactional
    public BranchRsDTO createBranch(BranchRqDTO branchRqDTO) throws BadRequestException {
        if(branchRepo.findById(branchRqDTO.getBranchCode()).isPresent())
            throw new BadRequestException("Branch Code Already Exist");
        if(branchRqDTO.getIsHeadOffice() && isHeadOfficeAlreadyExist())
            throw new BadRequestException("Head Office Already Exist");

        Branch branch = Branch.builder().branchCode(branchRqDTO.getBranchCode())
                        .branchName(branchRqDTO.getBranchName())
                        .isHeadOffice(branchRqDTO.getIsHeadOffice()).build();
        return buildBranchRsDTO(branchRepo.save(branch));
    }

    @Override
    public BranchRsDTO fetchBranchDetails(String branchCode) throws BranchNotFoundException {
        Optional<Branch> branch = branchRepo.findById(branchCode);
        if(branch.isEmpty())
            throw new BranchNotFoundException("Branch Not Found");
        return buildBranchRsDTO(branch.get());
    }

    @Override
    public List<BranchRsDTO> fetchAllBranchDetails() {
        List<Branch> branchList = branchRepo.findAll();
        return branchList.stream().map(this::buildBranchRsDTO).collect(Collectors.toList());
    }

    public Boolean isHeadOfficeAlreadyExist() {
        Branch branch = branchRepo.findByIsHeadOffice(Boolean.TRUE);
        if(branch != null)
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public BranchRsDTO buildBranchRsDTO(Branch branch) {
        return BranchRsDTO.builder().branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .isHeadOffice(branch.getIsHeadOffice())
                .build();
    }
}
