package com.bankfusion.corebanking.controller;

import com.bankfusion.corebanking.dto.CustomerRqDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.CustomerNotFoundException;
import com.bankfusion.corebanking.handler.ResponseHandler;
import com.bankfusion.corebanking.service.CustomerService;
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
    public ResponseEntity<Object> createCustomer(@RequestBody CustomerRqDTO customerRqDTO) throws BadRequestException {
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
    public ResponseEntity<Object> updateCustomerDetails(@RequestBody CustomerRqDTO customerRqDTO) throws CustomerNotFoundException {
        return ResponseHandler.generateSuccessResponse(customerService.updateCustomerDetails(customerRqDTO), HttpStatus.OK);
    }

}
