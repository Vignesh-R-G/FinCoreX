package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.BusinessDateDTO;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.BusinessDateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/businessDate")
public class BusinessDateController {
    @Autowired
    private BusinessDateService businessDateService;

    @GetMapping("/getBusinessDate")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> getBusinessDate(){
        return ResponseHandler.generateSuccessResponse(businessDateService.getBusinessDate(), HttpStatus.OK);
    }

    @PutMapping("/updateBusinessDate")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> updateBusinessDate(@Valid @RequestBody BusinessDateDTO businessDateDTO){
        return ResponseHandler.generateSuccessResponse(businessDateService.updateBusinessDate(businessDateDTO.getBusinessDate()), HttpStatus.OK);
    }
}
