package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.ProductRqDTO;
import com.fincorex.corebanking.dto.ProductRsDTO;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.ProductNotFoundException;

import java.util.List;

public interface ProductService {
    public ProductRsDTO createProduct(ProductRqDTO productRqDTO) throws BadRequestException;
    public ProductRsDTO fetchProductByID(String productID) throws ProductNotFoundException;
    public List<ProductRsDTO> fetchAllProducts();
}
