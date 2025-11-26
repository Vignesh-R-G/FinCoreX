package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.BusinessDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
public class BusinessDateController {
    @Autowired
    private BusinessDateService businessDateService;

    @GetMapping("/getBusinessDate")
    public ResponseEntity<Object> getBusinessDate(){
        return ResponseHandler.generateSuccessResponse(businessDateService.getBusinessDate(), HttpStatus.OK);
    }

    @PutMapping("/updateBusinessDate/{businessDate}")
    public ResponseEntity<Object> updateBusinessDate(@PathVariable("/businessDate") Date businessDate){
        return ResponseHandler.generateSuccessResponse(businessDateService.updateBusinessDate(businessDate), HttpStatus.OK);
    }
}
