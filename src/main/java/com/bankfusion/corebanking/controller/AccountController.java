package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.dto.AccountClosureRqDTO;
import com.bankfusion.corebanking.dto.OpenAccountRqDTO;
import com.bankfusion.corebanking.exception.*;
import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/openAccount")
    public ResponseEntity<Object> openAccount(@RequestBody OpenAccountRqDTO openAccountRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException, SubProductNotFoundException, BranchNotFoundException, CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.openAccount(openAccountRqDTO), HttpStatus.CREATED);
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
    public ResponseEntity<Object> closeAccount(@RequestBody AccountClosureRqDTO accountClosureRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.closeAccount(accountClosureRqDTO), HttpStatus.OK);
    }

    @GetMapping("/getAccountBalance/{accountID}")
    public ResponseEntity<Object> getAccountBalance(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.getAccountBalance(accountID), HttpStatus.OK);
    }

    @GetMapping("/getInterestDetails/{accountID}")
    public ResponseEntity<Object> getInterestDetails(@PathVariable("accountID") String accountID) throws AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(accountService.getInterestDetails(accountID), HttpStatus.OK);
    }
}
