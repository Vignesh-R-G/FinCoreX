package com.fincorex.corebanking.controller;

import com.fincorex.corebanking.dto.CustomerRqDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.CustomerNotFoundException;
import com.fincorex.corebanking.handler.ResponseHandler;
import com.fincorex.corebanking.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping("/createCustomer")
    public ResponseEntity<Object> createCustomer(@Valid @RequestBody CustomerRqDTO customerRqDTO) throws BadRequestException {
        return ResponseHandler.generateSuccessResponse(customerService.createCustomer(customerRqDTO), HttpStatus.CREATED);
    }

    @GetMapping("/fetchCustomer/{customerID}")
    public ResponseEntity<Object> fetchCustomerByID(@PathVariable("customerID") String customerID) throws CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(customerService.fetchCustomerByID(customerID), HttpStatus.OK);
    }

    @GetMapping("/fetchAllCustomers")
    public ResponseEntity<Object> fetchAllCustomers(){
        return ResponseHandler.generateSuccessResponse(customerService.fetchAllCustomers(), HttpStatus.OK);
    }

    @PutMapping("/updateCustomerDetails")
    public ResponseEntity<Object> updateCustomerDetails(@Valid @RequestBody CustomerRqDTO customerRqDTO) throws CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(customerService.updateCustomerDetails(customerRqDTO), HttpStatus.OK);
    }

}
