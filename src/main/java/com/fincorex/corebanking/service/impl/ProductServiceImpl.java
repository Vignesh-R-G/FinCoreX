package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.ProductRqDTO;
import com.fincorex.corebanking.dto.ProductRsDTO;
import com.fincorex.corebanking.entity.Product;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.ProductNotFoundException;
import com.fincorex.corebanking.repository.ProductRepo;
import com.fincorex.corebanking.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepo productRepo;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public ProductRsDTO createProduct(ProductRqDTO productRqDTO) throws BadRequestException {
        if(productRepo.findById(productRqDTO.getProductID()).isPresent())
            throw new BadRequestException("Product ID Already Exist");
        String productType = productRqDTO.getProductType();
        if(!productType.equals(ProductType.CA.name()) && !productType.equals(ProductType.SA.name()) && !productType.equals(ProductType.LENDING.name()) && !productType.equals(ProductType.INTERNAL.name())
            &&!productType.equals(ProductType.FD.name()))
            throw new BadRequestException("Invalid Product Type.Product Type should be CA/SA/LENDING/INTERNAL");
        Product product = Product.builder().productID(productRqDTO.getProductID())
                        .productName(productRqDTO.getProductName())
                        .productType(productType)
                        .build();
        return buildProductRsDTO(productRepo.save(product));
    }

    @Override
    public ProductRsDTO fetchProductByID(String productID) throws ProductNotFoundException {
        Optional<Product> product = productRepo.findById(productID);
        if(product.isEmpty())
            throw new ProductNotFoundException("Product Not Found");
        return buildProductRsDTO(product.get());
    }

    @Override
    public List<ProductRsDTO> fetchAllProducts() {
        List<Product> products = productRepo.findAll();
        return products.stream().map(this::buildProductRsDTO).collect(Collectors.toList());
    }

    public ProductRsDTO buildProductRsDTO(Product product){
        return ProductRsDTO.builder().productID(product.getProductID())
                .productName(product.getProductName())
                .productType(product.getProductType())
                .build();
    }
}
