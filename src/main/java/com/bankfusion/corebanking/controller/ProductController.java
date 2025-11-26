package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.dto.ProductRqDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.ProductNotFoundException;
import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/createProduct")
    public ResponseEntity<Object> createProduct(@RequestBody ProductRqDTO productRqDTO) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(productService.createProduct(productRqDTO), HttpStatus.CREATED);
    }

    @PostMapping("/fetchProductByID/{productID}")
    public ResponseEntity<Object> fetchProductByID(@PathVariable("productID") String productID) throws ProductNotFoundException {
        return ResponseHandler.generateSuccessResponse(productService.fetchProductByID(productID), HttpStatus.OK);
    }

    @PostMapping("/fetchAllProducts")
    public ResponseEntity<Object> fetchAllProducts() {
        return ResponseHandler.generateSuccessResponse(productService.fetchAllProducts(), HttpStatus.OK);
    }

}
