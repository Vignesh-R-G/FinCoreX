package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.FixtureRqDTO;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.FixtureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fixture")
public class FixtureController {
    @Autowired
    private FixtureService fixtureService;

    @PostMapping("/openFixedDeposit")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> openFixedDeposit(@Valid @RequestBody FixtureRqDTO fixtureRqDTO) throws SubProductNotFoundException, BranchNotFoundException, BadRequestException, CustomerNotFoundException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(fixtureService.openFixedDeposit(fixtureRqDTO), HttpStatus.CREATED);
    }

    @PostMapping("/fetchFixedDepositDetails/{accountID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchFixedDepositDetails(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(fixtureService.fetchFixedDepositDetails(accountID), HttpStatus.OK);
    }

    @PutMapping("/processFixtureBreakage/{accountID}")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> processFixtureBreakage(@PathVariable("accountID") String accountID) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(fixtureService.processFixtureBreakage(accountID), HttpStatus.OK);
    }

}
