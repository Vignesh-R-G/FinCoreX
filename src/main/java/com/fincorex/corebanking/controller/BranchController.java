package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.BranchRqDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.BranchNotFoundException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/branch")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @PostMapping("/createBranch")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> createBranch(@Valid @RequestBody BranchRqDTO branchRqDTO) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(branchService.createBranch(branchRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchBranchDetails/{branchCode}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> createBranch(@PathVariable("branchCode") String branchCode) throws BranchNotFoundException {
        return ResponseHandler.generateSuccessResponse(branchService.fetchBranchDetails(branchCode), HttpStatus.OK);
    }

    @GetMapping("/fetchAllBranchDetails")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchAllBranchDetails(){
        return ResponseHandler.generateSuccessResponse(branchService.fetchAllBranchDetails(), HttpStatus.OK);
    }
}
