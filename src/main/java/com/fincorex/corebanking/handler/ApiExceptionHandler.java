package com.fincorex.corebanking.handler;

import com.fincorex.corebanking.exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseHandler.generateFailureResponse(errors, HttpStatus.BAD_REQUEST);
    }

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

    @ExceptionHandler(DelinquencyProfileNotFoundException.class)
    public ResponseEntity<Object> delinquencyProfileNotFoundException(DelinquencyProfileNotFoundException delinquencyProfileNotFoundException){
        return ResponseHandler.generateFailureResponse(delinquencyProfileNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DelinquencyStageNotFoundException.class)
    public ResponseEntity<Object> delinquencyStageNotFoundException(DelinquencyStageNotFoundException delinquencyStageNotFoundException){
        return ResponseHandler.generateFailureResponse(delinquencyStageNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequestException(BadRequestException badRequestException){
        return ResponseHandler.generateFailureResponse(badRequestException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
