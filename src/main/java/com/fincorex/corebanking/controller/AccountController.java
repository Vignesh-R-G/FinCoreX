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
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/openAccount")
    public ResponseEntity<Object> openAccount(@Valid @RequestBody OpenAccountRqDTO openAccountRqDTO) throws SubProductNotFoundException, BranchNotFoundException, BadRequestException, CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.openAccount(openAccountRqDTO, Boolean.FALSE), HttpStatus.CREATED);
    }

    @GetMapping("/findAccountByID/{accountID}")
    public ResponseEntity<Object> findAccountByID(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountByID(accountID), HttpStatus.OK);
    }

    @GetMapping("/findAccountsByCustomer/{customerID}")
    public ResponseEntity<Object> findAccountsByCustomer(@PathVariable("customerID") String customerID, @RequestParam int pageNumber, @RequestParam int pageSize) throws SubProductNotFoundException, CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountsByCustomer(customerID, pageNumber, pageSize), HttpStatus.OK);
    }

    @GetMapping("/findAccountsBySubProduct/{subProductID}")
    public ResponseEntity<Object> findAccountsBySubProduct(@PathVariable("subProductID") String subProductID, @RequestParam int pageNumber, @RequestParam int pageSize) throws SubProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.findAccountsBySubProduct(subProductID, pageNumber, pageSize), HttpStatus.OK);
    }

    @PutMapping("/closeAccount")
    public ResponseEntity<Object> closeAccount(@Valid @RequestBody AccountClosureRqDTO accountClosureRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.closeAccount(accountClosureRqDTO), HttpStatus.OK);
    }

    @GetMapping("/getAccountBalance/{accountID}")
    public ResponseEntity<Object> getAccountBalance(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.getAccountBalance(accountID), HttpStatus.OK);
    }

    @GetMapping("/getInterestDetails/{accountID}")
    public ResponseEntity<Object> getInterestDetails(@PathVariable("accountID") String accountID) throws AccountNotFoundException, BadRequestException {
        return ResponseHandler.generateSuccessResponse(accountService.getInterestDetails(accountID), HttpStatus.OK);
    }
}
