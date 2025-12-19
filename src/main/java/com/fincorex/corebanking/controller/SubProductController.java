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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class SubProductController {
    @Autowired
    private SubProductService subProductService;

    @PostMapping("/createSubProduct")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> createSubProduct(@Valid @RequestBody ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.createSubProduct(productInheritanceRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchSubProductByID/{subProductID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchSubProductByID(@PathVariable("subProductID") String subProductID) throws SubProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductByID(subProductID), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductByCurrency/{currency}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchSubProductByCurrency(@PathVariable("currency") String currency) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductByCurrency(currency), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductsByProduct/{productID}")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchSubProductsByProduct(@PathVariable("productID") String productID) throws ProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductsByProduct(productID), HttpStatus.OK);
    }

    @GetMapping("/fetchSubProductsByProduct/{productID}/pagination")
    @PreAuthorize("hasAnyAuthority('TELLER','AUDITOR')")
    public ResponseEntity<Object> fetchSubProductsByProduct(@PathVariable("productID") String productID, @RequestParam int pageNumber,@RequestParam int pageSize) throws ProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.fetchSubProductsByProduct(productID, pageNumber, pageSize), HttpStatus.OK);
    }

    @PutMapping("/updateSubProductDetails")
    @PreAuthorize("hasAuthority('TELLER')")
    public ResponseEntity<Object> updateSubProductDetails(@Valid @RequestBody ProductInheritanceRqDTO productInheritanceRqDTO) throws ProductNotFoundException, SubProductNotFoundException, BadRequestException, AccountNotFoundException {
        return ResponseHandler.generateSuccessResponse(subProductService.updateSubProductDetails(productInheritanceRqDTO), HttpStatus.OK);
    }

}
