package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.ProductInheritanceRqDTO;
import com.fincorex.corebanking.dto.ProductInheritanceRsDTO;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.ProductNotFoundException;
import com.fincorex.corebanking.exception.SubProductNotFoundException;

import java.util.List;

public interface SubProductService {
    public ProductInheritanceRsDTO createSubProduct(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException;
    public ProductInheritanceRsDTO fetchSubProductByID(String subProductID) throws SubProductNotFoundException;
    public List<ProductInheritanceRsDTO> fetchSubProductByCurrency(String currency) throws BadRequestException;
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID) throws ProductNotFoundException;
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID,int pageNumber, int pageSize) throws ProductNotFoundException;
    public ProductInheritanceRsDTO updateSubProductDetails(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, SubProductNotFoundException, AccountNotFoundException;
}
