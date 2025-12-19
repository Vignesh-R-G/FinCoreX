package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.AccountClosureRqDTO;
import com.fincorex.corebanking.dto.OpenAccountRqDTO;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/openAccount")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> openAccount(@Valid @RequestBody OpenAccountRqDTO openAccountRqDTO) throws SubProductNotFoundException, BranchNotFoundException, BadRequestException, CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.openAccount(openAccountRqDTO, Boolean.FALSE, Boolean.FALSE), HttpStatus.CREATED);
    }

    @GetMapping("/findAccountByID/{accountID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> findAccountByID(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountByID(accountID), HttpStatus.OK);
    }

    @GetMapping("/findAccountsByCustomer/{customerID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> findAccountsByCustomer(@PathVariable("customerID") String customerID, @RequestParam int pageNumber, @RequestParam int pageSize) throws SubProductNotFoundException, CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountsByCustomer(customerID, pageNumber, pageSize), HttpStatus.OK);
    }

    @GetMapping("/findAccountsBySubProduct/{subProductID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> findAccountsBySubProduct(@PathVariable("subProductID") String subProductID, @RequestParam int pageNumber, @RequestParam int pageSize) throws SubProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountsBySubProduct(subProductID, pageNumber, pageSize), HttpStatus.OK);
    }

    @PutMapping("/closeAccount")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> closeAccount(@Valid @RequestBody AccountClosureRqDTO accountClosureRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.closeAccount(accountClosureRqDTO), HttpStatus.OK);
    }

    @GetMapping("/getAccountBalance/{accountID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> getAccountBalance(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.getAccountBalance(accountID), HttpStatus.OK);
    }

    @GetMapping("/getInterestDetails/{accountID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> getInterestDetails(@PathVariable("accountID") String accountID) throws AccountNotFoundException, BadRequestException {
        return ResponseHandler.generateSuccessResponse(accountService.getInterestDetails(accountID), HttpStatus.OK);
    }
}
