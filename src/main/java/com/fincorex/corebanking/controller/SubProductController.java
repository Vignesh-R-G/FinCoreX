package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.ProductInheritanceRqDTO;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.ProductNotFoundException;
import com.fincorex.corebanking.exception.SubProductNotFoundException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.SubProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SubProductController {
    @Autowired
    private SubProductService subProductService;

    @PostMapping("/createSubProduct")
    public ResponseEntity<Object> createSubProduct(@Valid @RequestBody ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.createSubProduct(productInheritanceRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchSubProductByID/{subProductID}")
    public ResponseEntity<Object> fetchSubProductByID(@PathVariable("subProductID") String subProductID) throws SubProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductByID(subProductID), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductByCurrency/{currency}")
    public ResponseEntity<Object> fetchSubProductByCurrency(@PathVariable("currency") String currency) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductByCurrency(currency), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductsByProduct/{productID}")
    public ResponseEntity<Object> fetchSubProductsByProduct(@PathVariable("productID") String productID) throws ProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductsByProduct(productID), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductsByProduct/{productID}/pagination")
    public ResponseEntity<Object> fetchSubProductsByProduct(@PathVariable("productID") String productID, @RequestParam int pageNumber,@RequestParam int pageSize) throws ProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductsByProduct(productID, pageNumber, pageSize), HttpStatus.OK);
    }

    @PutMapping("/updateSubProductDetails")
    public ResponseEntity<Object> updateSubProductDetails(@Valid @RequestBody ProductInheritanceRqDTO productInheritanceRqDTO) throws ProductNotFoundException, SubProductNotFoundException, BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.updateSubProductDetails(productInheritanceRqDTO), HttpStatus.OK);
    }

}
