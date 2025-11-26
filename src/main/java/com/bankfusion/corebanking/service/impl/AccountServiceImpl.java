package com.bankfusion.corebanking.service.impl;

import com.bankfusion.corebanking.constants.ApiConstants;
import com.bankfusion.corebanking.dto.*;
import com.bankfusion.corebanking.entity.Account;
import com.bankfusion.corebanking.entity.Branch;
import com.bankfusion.corebanking.entity.Customer;
import com.bankfusion.corebanking.entity.ProductInheritance;
import com.bankfusion.corebanking.exception.*;
import com.bankfusion.corebanking.repository.AccountRepo;
import com.bankfusion.corebanking.repository.BranchRepo;
import com.bankfusion.corebanking.repository.CustomerRepo;
import com.bankfusion.corebanking.repository.ProductInheritanceRepo;
import com.bankfusion.corebanking.service.AccountService;
import com.bankfusion.corebanking.utils.BusinessDateUtil;
import com.bankfusion.corebanking.utils.InterestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private SubProductServiceImpl subProductService;

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    @Autowired
    private BranchServiceImpl branchService;

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ProductInheritanceRepo productInheritanceRepo;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public AccountRsDTO openAccount(OpenAccountRqDTO openAccountRqDTO) throws BranchNotFoundException, CustomerNotFoundException, SubProductNotFoundException {
        Optional<Branch> branch = branchRepo.findById(openAccountRqDTO.getBranchCode());
        if(branch.isEmpty())
            throw new BranchNotFoundException("Branch Not Found");
        Optional<Customer> customer = customerRepo.findById(openAccountRqDTO.getCustomerID());
        if(customer.isEmpty())
            throw new CustomerNotFoundException("Customer Not Found");
        Optional<ProductInheritance> productInheritance = productInheritanceRepo.findById(openAccountRqDTO.getSubProductID());
        if(productInheritance.isEmpty())
            throw new SubProductNotFoundException("Sub-Product Not Found");
        Account account = Account.builder()
                .accountID(openAccountRqDTO.getAccountID())
                .blockedBalance(BigDecimal.ZERO)
                .clearedBalance(BigDecimal.ZERO)
                .creditInterestRate(productInheritance.get().getCreditInterestRate())
                .debitInterestRate(productInheritance.get().getDebitInterestRate())
                .isClosed(Boolean.FALSE)
                .creditAccruedInterest(BigDecimal.ZERO)
                .debitAccruedInterest(BigDecimal.ZERO)
                .branch(branch.get())
                .customer(customer.get())
                .subProduct(productInheritance.get())
                .openDate(businessDateUtil.getCurrentBusinessDate())
                .build();

        if(InterestUtils.isInterestAccrualProduct(account.getSubProduct())) {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_INITIALIZE_EVENT, openAccountRqDTO.getAccountID(), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE));
        }
        return buildAccountRsDTO(account);
    }

    @Override
    public AccountRsDTO findAccountByID(String accountID) throws AccountNotFoundException {
        Optional<Account> account = accountRepo.findById(accountID);
        if(account.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        return buildAccountRsDTO(account.get());
    }

    @Override
    public List<AccountRsDTO> findAccountsByCustomer(String customerID, int pageNumber, int pageSize) throws CustomerNotFoundException {
        Optional<Customer> customer = customerRepo.findById(customerID);
        if(customer.isEmpty())
            throw new CustomerNotFoundException("Customer Not Found");
        Pageable pageable = PageRequest.of(pageNumber,pageSize, Sort.by("openDate").descending());
        Page<Account> page = accountRepo.findAllByCustomer(customer.get(),pageable);
        if(page == null || page.isEmpty())
            return new ArrayList<>();
        return page.getContent().stream().map(this::buildAccountRsDTO).collect(Collectors.toList());
    }

    @Override
    public List<AccountRsDTO> findAccountsBySubProduct(String subProductID, int pageNumber, int pageSize) throws SubProductNotFoundException {
        Optional<ProductInheritance> productInheritance = productInheritanceRepo.findById(subProductID);
        if(productInheritance.isEmpty())
            throw new SubProductNotFoundException("Sub-Product Not Found");
        Pageable pageable = PageRequest.of(pageNumber,pageSize, Sort.by("openDate").descending());
        Page<Account> page = accountRepo.findAllBySubProduct(productInheritance.get(),pageable);
        if(page == null || page.isEmpty())
            return new ArrayList<>();
        return page.getContent().stream().map(this::buildAccountRsDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountRsDTO closeAccount(AccountClosureRqDTO accountClosureRqDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(accountClosureRqDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(account.getIsClosed())
            throw new BadRequestException("Account Already Closed");
        account.setIsClosed(Boolean.TRUE);
        account.setClosureDate(businessDateUtil.getCurrentBusinessDate());
        account.setClosureReason(accountClosureRqDTO.getClosureReason());
        return buildAccountRsDTO(accountRepo.save(account));
    }

    public BalanceEnquiryRsDTO getAccountBalance(String accountID) throws AccountNotFoundException {
        Optional<Account> accountOptional = accountRepo.findById(accountID);
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        return BalanceEnquiryRsDTO.builder()
                .clearedBalance(account.getClearedBalance())
                .blockedBalance(account.getBlockedBalance())
                .availableBalance(account.getClearedBalance().subtract(account.getBlockedBalance()))
                .build();
    }

    @Override
    public InterestEnquiryRsDTO getInterestDetails(String accountID) throws AccountNotFoundException {
        Optional<Account> accountOptional = accountRepo.findById(accountID);
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        BigDecimal creditAccruedInterest = InterestUtils.calculateAccruedInterest(account.getClearedBalance(), account.getCreditInterestRate(),
                account.getCreditAccruedInterest(), account.getLastAccrualDate(), businessDateUtil.getCurrentBusinessDate());
        BigDecimal debitAccruedInterest = InterestUtils.calculateAccruedInterest(account.getClearedBalance(), account.getDebitInterestRate(),
                account.getDebitAccruedInterest(), account.getLastAccrualDate(), businessDateUtil.getCurrentBusinessDate());

        return InterestEnquiryRsDTO.builder()
                .accountID(accountID)
                .creditInterestRate(account.getCreditInterestRate())
                .debitInterestRate(account.getDebitInterestRate())
                .creditAccruedInterest(creditAccruedInterest)
                .debitAccruedInterest(debitAccruedInterest)
                .build();
    }

    public AccountRsDTO buildAccountRsDTO(Account account){
        return AccountRsDTO.builder()
                .accountID(account.getAccountID())
                .blockedBalance(account.getBlockedBalance())
                .clearedBalance(account.getClearedBalance())
                .closureDate(account.getClosureDate())
                .creditAccruedInterest(account.getCreditAccruedInterest())
                .creditInterestRate(account.getCreditInterestRate())
                .openDate(account.getOpenDate())
                .isClosed(account.getIsClosed())
                .debitInterestRate(account.getDebitInterestRate())
                .debitAccruedInterest(account.getDebitAccruedInterest())
                .lastAccrualDate(account.getLastAccrualDate())
                .branch(branchService.buildBranchRsDTO(account.getBranch()))
                .customer(customerService.buildCustomerRsDTO(account.getCustomer()))
                .subProduct(subProductService.buildSubProductRsDTO(account.getSubProduct()))
                .closureReason(account.getClosureReason())
                .build();
    }
}
