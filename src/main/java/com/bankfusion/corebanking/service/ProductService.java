package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.ProductRqDTO;
import com.bankfusion.corebanking.dto.ProductRsDTO;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.ProductNotFoundException;

import java.util.List;

public interface ProductService {
    public ProductRsDTO createProduct(ProductRqDTO productRqDTO) throws BadRequestException;
    public ProductRsDTO fetchProductByID(String productID) throws ProductNotFoundException;
    public List<ProductRsDTO> fetchAllProducts();
}
