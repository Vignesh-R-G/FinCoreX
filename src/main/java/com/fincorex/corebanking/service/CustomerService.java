package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.CustomerRqDTO;
import com.fincorex.corebanking.dto.CustomerRsDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.CustomerNotFoundException;

import java.util.List;


public interface CustomerService {
    public CustomerRsDTO createCustomer(CustomerRqDTO customerRqDTO) throws BadRequestException;
    public CustomerRsDTO fetchCustomerByID(String customerID) throws CustomerNotFoundException;
    public List<CustomerRsDTO> fetchAllCustomers();
    public CustomerRsDTO updateCustomerDetails(CustomerRqDTO customerRqDTO) throws CustomerNotFoundException;
}
