package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.ProductInheritanceRqDTO;
import com.fincorex.corebanking.dto.ProductInheritanceRsDTO;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.Product;
import com.fincorex.corebanking.entity.ProductInheritance;
import com.fincorex.corebanking.enums.Currency;
import com.fincorex.corebanking.enums.InterestMethod;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.ProductNotFoundException;
import com.fincorex.corebanking.exception.SubProductNotFoundException;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.ProductInheritanceRepo;
import com.fincorex.corebanking.repository.ProductRepo;
import com.fincorex.corebanking.service.SubProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubProductServiceImpl implements SubProductService {

    @Autowired
    private ProductServiceImpl productServiceImpl;

    @Autowired
    private ProductInheritanceRepo productInheritanceRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public ProductInheritanceRsDTO createSubProduct(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, AccountNotFoundException {
        validateSubProductDetails(productInheritanceRqDTO);

        ProductInheritance productInheritance = ProductInheritance.builder()
                .productContextCode(productInheritanceRqDTO.getProductContextCode())
                .product(productRepo.findById(productInheritanceRqDTO.getProductID()).get())
                .pnlAccount(productInheritanceRqDTO.getPnlAccount())
                .glAccount(productInheritanceRqDTO.getGlAccount())
                .isoCurrencyCode(productInheritanceRqDTO.getIsoCurrencyCode())
                .debitInterestRate(productInheritanceRqDTO.getDebitInterestRate())
                .creditInterestRate(productInheritanceRqDTO.getCreditInterestRate())
                .interestMethod(productInheritanceRqDTO.getInterestMethod())
                .build();
        return buildSubProductRsDTO(productInheritanceRepo.save(productInheritance));
    }

    @Override
    public ProductInheritanceRsDTO fetchSubProductByID(String subProductID) throws SubProductNotFoundException {
        Optional<ProductInheritance> productInheritance = productInheritanceRepo.findById(subProductID);
        if(productInheritance.isEmpty())
            throw new SubProductNotFoundException("Sub Product Not Found");
        return buildSubProductRsDTO(productInheritance.get());
    }

    @Override
    public List<ProductInheritanceRsDTO> fetchSubProductByCurrency(String currency) throws BadRequestException {
        List<Currency> currencies = Arrays.stream(Currency.values()).filter(curr->curr.name().equals(currency)).collect(Collectors.toList());
        if(currencies.isEmpty())
            throw new BadRequestException("Currency is Invalid");

        List<ProductInheritance> productInheritanceList = productInheritanceRepo.findAllByIsoCurrencyCode(currency);
        return productInheritanceList.stream().map(this::buildSubProductRsDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID) throws ProductNotFoundException {
        Optional<Product> product = productRepo.findById(productID);
        if(product.isEmpty())
            throw new ProductNotFoundException("Product Not Found");
        List<ProductInheritance> productInheritanceList = productInheritanceRepo.findByProduct(product.get());
        return productInheritanceList.stream().map(this::buildSubProductRsDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductInheritanceRsDTO> fetchSubProductsByProduct(String productID, int pageNumber, int pageSize) throws ProductNotFoundException {
        Optional<Product> product = productRepo.findById(productID);
        if(product.isEmpty())
            throw new ProductNotFoundException("Product Not Found");
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<ProductInheritance> page = productInheritanceRepo.findAllByProduct(product.get(),pageable);
        if(page == null || page.isEmpty())
            return new ArrayList<>();
        return page.getContent().stream().map(productInheritance-> buildSubProductRsDTO(productInheritance)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public ProductInheritanceRsDTO updateSubProductDetails(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException, ProductNotFoundException, SubProductNotFoundException, AccountNotFoundException {
        validateSubProductDetails(productInheritanceRqDTO);
        Optional<ProductInheritance> subProduct = productInheritanceRepo.findById(productInheritanceRqDTO.getProductContextCode());
        if(subProduct.isEmpty())
            throw new SubProductNotFoundException("Sub Product Not Found");
        if(!subProduct.get().getInterestMethod().equals(productInheritanceRqDTO.getInterestMethod()))
            throw new BadRequestException("Interest Method cannot be updated under sub-product level");
        if(!subProduct.get().getIsoCurrencyCode().equals(productInheritanceRqDTO.getIsoCurrencyCode()))
            throw new BadRequestException("Currency Code cannot be updated under sub-product level");

        ProductInheritance productInheritance = subProduct.get();
        productInheritance.setProduct(productRepo.findById(productInheritanceRqDTO.getProductID()).get());
        productInheritance.setPnlAccount(productInheritanceRqDTO.getPnlAccount());
        productInheritance.setGlAccount(productInheritanceRqDTO.getGlAccount());
        productInheritance.setIsoCurrencyCode(productInheritanceRqDTO.getIsoCurrencyCode());
        productInheritance.setDebitInterestRate(productInheritanceRqDTO.getDebitInterestRate());
        productInheritance.setCreditInterestRate(productInheritanceRqDTO.getCreditInterestRate());

        return buildSubProductRsDTO(productInheritanceRepo.save(productInheritance));
    }

    public void validateSubProductDetails(ProductInheritanceRqDTO productInheritanceRqDTO) throws ProductNotFoundException, AccountNotFoundException, BadRequestException {
        BigDecimal debitInterestRate = productInheritanceRqDTO.getDebitInterestRate();
        BigDecimal creditInterestRate = productInheritanceRqDTO.getCreditInterestRate();

        Optional<Product> product = productRepo.findById(productInheritanceRqDTO.getProductID());
        if(product.isEmpty())
            throw new ProductNotFoundException("Product Not Found");

        if(product.get().getProductType().equals(ProductType.INTERNAL.name())){
            if(productInheritanceRqDTO.getPnlAccount() != null && !productInheritanceRqDTO.getPnlAccount().equals(""))
                throw new BadRequestException("PNL Account should not be provided for the Internal Sub-Product Creation");
            if(productInheritanceRqDTO.getGlAccount() != null && !productInheritanceRqDTO.getGlAccount().equals(""))
                throw new BadRequestException("GL Account should not be provided for the Internal Sub-Product Creation");

        } else {
            Optional<Account> pnlAccount = accountRepo.findById(productInheritanceRqDTO.getPnlAccount());
            if (pnlAccount.isEmpty())
                throw new AccountNotFoundException("PNL Account Not Found");
            if (!pnlAccount.get().getSubProduct().getProduct().getProductType().equals(ProductType.INTERNAL.name()))
                throw new BadRequestException("PNL Account should be an Internal Account");

            Optional<Account> glAccount = accountRepo.findById(productInheritanceRqDTO.getGlAccount());
            if (glAccount.isEmpty())
                throw new AccountNotFoundException("GL Account Not Found");
            if (!glAccount.get().getSubProduct().getProduct().getProductType().equals(ProductType.INTERNAL.name()))
                throw new BadRequestException("GL Account should be an Internal Account");
        }

        List<Currency> currencies = Arrays.stream(Currency.values()).filter(curr->curr.name().equals(productInheritanceRqDTO.getIsoCurrencyCode())).collect(Collectors.toList());
        if(currencies.isEmpty())
            throw new BadRequestException("Currency is Invalid");

        if(product.get().getProductType().equals(ProductType.INTERNAL.name()) && (!debitInterestRate.equals(BigDecimal.ZERO) || !creditInterestRate.equals(BigDecimal.ZERO)))
            throw new BadRequestException("Interest Rates should be zero for the Internal Product");
        if(product.get().getProductType().equals(ProductType.SA.name()) && (!debitInterestRate.equals(BigDecimal.ZERO)))
            throw new BadRequestException("Debit Interest Rates should be zero for the Savings Product");
        if(product.get().getProductType().equals(ProductType.LENDING.name()) && (!creditInterestRate.equals(BigDecimal.ZERO)))
            throw new BadRequestException("Credit Interest Rates should be zero for the Lending Product");

        List<InterestMethod> interestMethods = Arrays.stream(InterestMethod.values()).filter(interestMethod->interestMethod.name().equals(productInheritanceRqDTO.getInterestMethod())).collect(Collectors.toList());
        if(interestMethods.isEmpty())
            throw new BadRequestException("Interest Method is Invalid");
        if(!product.get().getProductType().equals(ProductType.LENDING.name()) && productInheritanceRqDTO.getInterestMethod().equals(InterestMethod.FIXED.name()))
            throw new BadRequestException("Fixed Interest Method is not supported for non-lending products");
        if(productInheritanceRqDTO.getInterestMethod().equals(InterestMethod.FIXED.name()) && (!debitInterestRate.equals(BigDecimal.ZERO) || !creditInterestRate.equals(BigDecimal.ZERO)))
            throw new BadRequestException("Interest Rates should be zero for the Fixed Interest Debit Interest Method");

        validatePnlAndGlCombination(productInheritanceRqDTO);
    }

    private void validatePnlAndGlCombination(ProductInheritanceRqDTO productInheritanceRqDTO) throws BadRequestException {
        String pnlAccount = productInheritanceRqDTO.getPnlAccount();
        String glAccount = productInheritanceRqDTO.getGlAccount();
        List<ProductInheritance> pnlAccountProductList = productInheritanceRepo.findAllByPnlAccount(productInheritanceRqDTO.getPnlAccount());
        for(ProductInheritance productInheritance : pnlAccountProductList){
            if(!productInheritance.getProductContextCode().equals(productInheritanceRqDTO.getProductContextCode())) {
                if (!productInheritance.getGlAccount().equals(glAccount)) {
                    throw new BadRequestException("Different PNL Account and GL Account Combination Already Exist");
                }
            }
        }

        List<ProductInheritance> glAccountProductList = productInheritanceRepo.findAllByGlAccount(productInheritanceRqDTO.getGlAccount());
        for(ProductInheritance productInheritance : glAccountProductList){
            if(!productInheritance.getProductContextCode().equals(productInheritanceRqDTO.getProductContextCode())) {
                if (!productInheritance.getPnlAccount().equals(pnlAccount)) {
                    throw new BadRequestException("Different PNL Account and GL Account Combination Already Exist");
                }
            }
        }
    }

    public ProductInheritanceRsDTO buildSubProductRsDTO(ProductInheritance productInheritance){
        return ProductInheritanceRsDTO.builder()
                .productContextCode(productInheritance.getProductContextCode())
                .debitInterestRate(productInheritance.getDebitInterestRate())
                .creditInterestRate(productInheritance.getCreditInterestRate())
                .glAccount(productInheritance.getGlAccount())
                .pnlAccount(productInheritance.getPnlAccount())
                .isoCurrencyCode(productInheritance.getIsoCurrencyCode())
                .interestMethod(productInheritance.getInterestMethod())
                .product(productServiceImpl.buildProductRsDTO(productInheritance.getProduct()))
                .build();
    }
}
