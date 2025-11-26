package com.bankfusion.corebanking.handler;

import com.bankfusion.corebanking.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Object> customerNotFoundException(CustomerNotFoundException customerNotFoundException){
        return ResponseHandler.generateFailureResponse(customerNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<Object> branchNotFoundException(BranchNotFoundException branchNotFoundException){
        return ResponseHandler.generateFailureResponse(branchNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> productNotFoundException(ProductNotFoundException productNotFoundException){
        return ResponseHandler.generateFailureResponse(productNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SubProductNotFoundException.class)
    public ResponseEntity<Object> subProductNotFoundException(SubProductNotFoundException subProductNotFoundException){
        return ResponseHandler.generateFailureResponse(subProductNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> accountNotFoundException(AccountNotFoundException accountNotFoundException){
        return ResponseHandler.generateFailureResponse(accountNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Object> transactionNotFoundException(TransactionNotFoundException transactionNotFoundException){
        return ResponseHandler.generateFailureResponse(transactionNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequestException(BadRequestException badRequestException){
        return ResponseHandler.generateFailureResponse(badRequestException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
