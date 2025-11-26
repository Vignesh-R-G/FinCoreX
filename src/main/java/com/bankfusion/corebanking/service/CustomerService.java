package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.CustomerRqDTO;
import com.bankfusion.corebanking.dto.CustomerRsDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.CustomerNotFoundException;

import java.util.List;


public interface CustomerService {
    public CustomerRsDTO createCustomer(CustomerRqDTO customerRqDTO) throws BadRequestException;
    public CustomerRsDTO fetchCustomerByID(String customerID) throws CustomerNotFoundException;
    public List<CustomerRsDTO> fetchAllCustomers();
    public CustomerRsDTO updateCustomerDetails(CustomerRqDTO customerRqDTO) throws CustomerNotFoundException;
}
