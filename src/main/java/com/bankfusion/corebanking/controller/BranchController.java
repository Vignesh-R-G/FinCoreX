package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.dto.BranchRqDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.BranchNotFoundException;
import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/branch")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @PostMapping("/createBranch")
    public ResponseEntity<Object> createBranch(@RequestBody BranchRqDTO branchRqDTO) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(branchService.createBranch(branchRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchBranchDetails/{branchCode}")
    public ResponseEntity<Object> createBranch(@PathVariable("branchCode") String branchCode) throws BranchNotFoundException {
        return ResponseHandler.generateSuccessResponse(branchService.fetchBranchDetails(branchCode), HttpStatus.OK);
    }

    @GetMapping("/fetchAllBranchDetails")
    public ResponseEntity<Object> fetchAllBranchDetails(){
        return ResponseHandler.generateSuccessResponse(branchService.fetchAllBranchDetails(), HttpStatus.OK);
    }
}
