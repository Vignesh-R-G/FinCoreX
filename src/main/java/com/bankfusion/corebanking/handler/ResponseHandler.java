package com.bankfusion.corebanking.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public class ResponseHandler {
    private static final String DATA = "data";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String STATUS = "status";


    public static ResponseEntity<Object> generateSuccessResponse(Object object,HttpStatus status){
        HashMap<String,Object> response = new HashMap<>();
        response.put(SUCCESS,Boolean.TRUE);
        response.put(STATUS,status);
        response.put(DATA,object);
        return new ResponseEntity<>(response,status);
    }

    public static ResponseEntity<Object> generateFailureResponse(Object object,HttpStatus status){
        HashMap<String,Object> response = new HashMap<>();
        response.put(SUCCESS,Boolean.FALSE);
        response.put(STATUS,status);
        response.put(ERROR,object);
        return new ResponseEntity<>(response,status);
    }
}
