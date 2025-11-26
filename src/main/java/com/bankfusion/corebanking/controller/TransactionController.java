package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.dto.*;
import com.bankfusion.corebanking.exception.AccountNotFoundException;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.TransactionNotFoundException;
import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/processCASATransaction")
    public ResponseEntity<Object> processCASATransaction(@RequestBody TransactionRqDTO transactionRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processCASATransaction(transactionRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchTransactionByID/{transactionID}")
    public ResponseEntity<Object> fetchTransactionByID(@PathVariable("transactionID") String transactionID) throws TransactionNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.fetchTransactionByID(transactionID), HttpStatus.OK);
    }

    @GetMapping("/blockAmount")
    public ResponseEntity<Object> blockAmount(@RequestBody BlockTransactionDTO blockTransactionDTO) throws AccountNotFoundException, BadRequestException {
        return ResponseHandler.generateSuccessResponse(transactionService.blockAmount(blockTransactionDTO), HttpStatus.OK);
    }

    @GetMapping("/unBlockAmount")
    public ResponseEntity<Object> unBlockAmount(@RequestBody UnBlockTransactionDTO unBlockTransactionDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.unBlockAmount(unBlockTransactionDTO), HttpStatus.OK);
    }

    @PutMapping("/processInterestApplication")
    public ResponseEntity<Object> processInterestApplication(@RequestBody InterestApplicationRqDTO interestApplicationRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processInterestApplication(interestApplicationRqDTO), HttpStatus.OK);
    }

    @PutMapping("/processInterestRateChange")
    public ResponseEntity<Object> processInterestRateChange(@RequestBody InterestRateChangeRqDTO interestRateChangeRqDTO) throws BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(transactionService.processInterestRateChange(interestRateChangeRqDTO), HttpStatus.OK);
    }

}
