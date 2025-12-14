package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.DelinquencyProfileRqDTO;
import com.fincorex.corebanking.dto.DelinquencyStageRqDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.DelinquencyProfileNotFoundException;
import com.fincorex.corebanking.exception.DelinquencyStageNotFoundException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.DelinquencyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DelinquencyController {
    @Autowired
    private DelinquencyService delinquencyService;

    @PostMapping("/createDelinquencyProfile")
    public ResponseEntity<Object> createDelinquencyProfile(@Valid @RequestBody DelinquencyProfileRqDTO delinquencyProfileRqDTO) {
        return ResponseHandler.generateSuccessResponse(delinquencyService.createDelinquencyProfile(delinquencyProfileRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchDelinquencyDetails/{delinquencyProfileID}")
    public ResponseEntity<Object> fetchDelinquencyDetails(@PathVariable("delinquencyProfileID") Long delinquencyProfileID) throws DelinquencyProfileNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.fetchDelinquencyDetails(delinquencyProfileID), HttpStatus.OK);
    }

    @PutMapping("/updateDelinquencyProfile/{delinquencyProfileID}")
    public ResponseEntity<Object> updateDelinquencyProfile(@PathVariable("delinquencyProfileID") Long delinquencyProfileID, @RequestBody DelinquencyProfileRqDTO delinquencyProfileRqDTO) throws DelinquencyProfileNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.updateDelinquencyProfile(delinquencyProfileID, delinquencyProfileRqDTO), HttpStatus.OK);
    }

    @PostMapping("/createDelinquencyStage")
    public ResponseEntity<Object> createDelinquencyStage(@Valid @RequestBody DelinquencyStageRqDTO delinquencyStageRqDTO) throws BadRequestException, DelinquencyProfileNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.createDelinquencyStage(delinquencyStageRqDTO), HttpStatus.CREATED);
    }

    @PostMapping("/createBulkDelinquencyStage")
    public ResponseEntity<Object> createBulkDelinquencyStage(@Valid @RequestBody List<DelinquencyStageRqDTO> delinquencyStageRqDTOList) throws BadRequestException, DelinquencyProfileNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.createBulkDelinquencyStage(delinquencyStageRqDTOList), HttpStatus.CREATED);
    }

    @GetMapping("/fetchDelinquencyStage/{delinquencyStageID}")
    public ResponseEntity<Object> fetchDelinquencyStage(@PathVariable("delinquencyStageID") Long delinquencyStageID) throws DelinquencyStageNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.fetchDelinquencyStage(delinquencyStageID), HttpStatus.OK);
    }

    @PutMapping("/updateDelinquencyStage/{delinquencyStageID}")
    public ResponseEntity<Object> updateDelinquencyStage(@PathVariable("delinquencyStageID") Long delinquencyStageID, @Valid @RequestBody DelinquencyStageRqDTO delinquencyStageRqDTO) throws BadRequestException, DelinquencyStageNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.updateDelinquencyStage(delinquencyStageID, delinquencyStageRqDTO), HttpStatus.OK);
    }

    @DeleteMapping("/deleteDelinquencyStage/{delinquencyStageID}")
    public ResponseEntity<Object> deleteDelinquencyStage(@PathVariable("delinquencyStageID") Long delinquencyStageID) throws BadRequestException, DelinquencyStageNotFoundException {
        return ResponseHandler.generateSuccessResponse(delinquencyService.deleteDelinquencyStage(delinquencyStageID), HttpStatus.OK);
    }
}
