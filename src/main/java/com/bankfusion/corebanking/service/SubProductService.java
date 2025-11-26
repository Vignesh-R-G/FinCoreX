package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.ProductInheritanceRqDTO;
import com.bankfusion.corebanking.dto.ProductInheritanceRsDTO;
import com.bankfusion.corebanking.exception.AccountNotFoundException;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.ProductNotFoundException;
import com.bankfusion.corebanking.exception.SubProductNotFoundException;

import java.util.List;

public interface SubProductService {
    public ProductInheritanceRsDTO createSubProduct(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException;
    public ProductInheritanceRsDTO fetchSubProductByID(String subProductID) throws SubProductNotFoundException;
    public List<ProductInheritanceRsDTO> fetchSubProductByCurrency(String currency) throws BadRequestException;
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID) throws ProductNotFoundException;
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID,int pageNumber, int pageSize) throws ProductNotFoundException;
    public ProductInheritanceRsDTO updateSubProductDetails(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, SubProductNotFoundException, AccountNotFoundException;
}
