package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.LoanEstablishmentRqDTO;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lending")
public class LendingController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/establishLoan")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> establishLoan(@Valid @RequestBody LoanEstablishmentRqDTO loanEstablishmentRqDTO) throws SubProductNotFoundException, BranchNotFoundException, BadRequestException, DelinquencyProfileNotFoundException, CustomerNotFoundException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(loanService.establishLoan(loanEstablishmentRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchLoanDetails/{loanAccountID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchLoanDetails(@PathVariable("loanAccountID") String loanAccountID) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(loanService.fetchLoanDetails(loanAccountID), HttpStatus.OK);
    }

    @GetMapping("/settleLoanAccount/{loanAccountID}")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> settleLoanAccount(@PathVariable("loanAccountID") String loanAccountID) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(loanService.settleLoanAccount(loanAccountID), HttpStatus.OK);
    }
}
