package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.TransactionNotFoundException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/processTransaction")
    public ResponseEntity<Object> processTransaction(@Valid @RequestBody TransactionRqDTO transactionRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processTransaction(transactionRqDTO, TransactionCode.A00), HttpStatus.CREATED);
    }

    @GetMapping("/fetchTransactionByID/{transactionID}")
    public ResponseEntity<Object> fetchTransactionByID(@PathVariable("transactionID") String transactionID) throws TransactionNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.fetchTransactionByID(transactionID), HttpStatus.OK);
    }

    @PutMapping("/blockAmount")
    public ResponseEntity<Object> blockAmount(@Valid @RequestBody BlockTransactionDTO blockTransactionDTO) throws AccountNotFoundException, BadRequestException {
        return ResponseHandler.generateSuccessResponse(transactionService.blockAmount(blockTransactionDTO), HttpStatus.OK);
    }

    @PutMapping("/unBlockAmount")
    public ResponseEntity<Object> unBlockAmount(@Valid @RequestBody UnBlockTransactionDTO unBlockTransactionDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.unBlockAmount(unBlockTransactionDTO), HttpStatus.OK);
    }

    @PutMapping("/processInterestApplication")
    public ResponseEntity<Object> processInterestApplication(@Valid @RequestBody InterestApplicationRqDTO interestApplicationRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processInterestApplication(interestApplicationRqDTO), HttpStatus.OK);
    }

    @PutMapping("/processInterestRateChange")
    public ResponseEntity<Object> processInterestRateChange(@Valid @RequestBody InterestRateChangeRqDTO interestRateChangeRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processInterestRateChange(interestRateChangeRqDTO), HttpStatus.OK);
    }

}
