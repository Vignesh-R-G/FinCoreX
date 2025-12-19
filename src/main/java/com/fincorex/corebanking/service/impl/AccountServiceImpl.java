package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.entity.*;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.repository.*;
import com.fincorex.corebanking.service.AccountService;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import com.fincorex.corebanking.utils.EmailUtil;
import com.fincorex.corebanking.utils.InterestUtils;
import com.fincorex.corebanking.utils.TransactionUtils;
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

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private LendingBlocksRepo lendingBlocksRepo;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    @Autowired
    private FixtureRepo fixtureRepo;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    // If it is a checked Exception, then we have to explicitly mention it
    // By default, Spring auto roll back only for unchecked Exception
    public synchronized AccountRsDTO openAccount(OpenAccountRqDTO openAccountRqDTO, Boolean isInvokedFromLoanService, Boolean isInvokedFromFixtureService) throws BranchNotFoundException, CustomerNotFoundException, SubProductNotFoundException, BadRequestException {
        Optional<Branch> branch = branchRepo.findById(openAccountRqDTO.getBranchCode());
        if(branch.isEmpty())
            throw new BranchNotFoundException("Branch Not Found");
        Optional<ProductInheritance> productInheritance = productInheritanceRepo.findById(openAccountRqDTO.getSubProductID());
        if(productInheritance.isEmpty())
            throw new SubProductNotFoundException("Sub-Product Not Found");
        if(!isInvokedFromLoanService && productInheritance.get().getProduct().getProductType().equals(ProductType.LENDING.name()))
            throw new BadRequestException("Loan Accounts should be established using the Loan Establishment API");
        if(isInvokedFromLoanService && !productInheritance.get().getProduct().getProductType().equals(ProductType.LENDING.name()))
            throw new BadRequestException("Invalid Sub-Product. Loan Sub-Product should be provided to establish a loan");

        if(!isInvokedFromFixtureService && productInheritance.get().getProduct().getProductType().equals(ProductType.FD.name()))
            throw new BadRequestException("Fixed Deposits should be created using the Fixed Deposit API API");
        if(isInvokedFromFixtureService && !productInheritance.get().getProduct().getProductType().equals(ProductType.FD.name()))
            throw new BadRequestException("Invalid Sub-Product. Fixed Deposits Sub-Product should be provided to establish an FD Account");

        Optional<Customer> customer = customerRepo.findById(openAccountRqDTO.getCustomerID());

        // Validate Customer only if it is a non-internal product
        String productType = productInheritance.get().getProduct().getProductType();
        if(!productType.equals(ProductType.INTERNAL.name()) && customer.isEmpty())
            throw new CustomerNotFoundException("Customer Not Found");

        String accountID = "";
        if(!productType.equals(ProductType.INTERNAL.name()))
            accountID = generateAccountID(customer.get());
        else
            accountID = generateAccountID(branch.get());

        Account account = Account.builder()
            .accountID(accountID)
            .blockedBalance(BigDecimal.ZERO)
            .clearedBalance(BigDecimal.ZERO)
            .creditInterestRate(productInheritance.get().getCreditInterestRate())
            .debitInterestRate(productInheritance.get().getDebitInterestRate())
            .isClosed(Boolean.FALSE)
            .creditAccruedInterest(BigDecimal.ZERO)
            .debitAccruedInterest(BigDecimal.ZERO)
            .branch(branch.get())
            .customer(productType.equals(ProductType.INTERNAL.name()) ? null : customer.get())
            .subProduct(productInheritance.get())
            .openDate(businessDateUtil.getCurrentBusinessDate())
            .build();

        if(InterestUtils.isInterestAccrualProduct(account.getSubProduct())) {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_INITIALIZE_EVENT, accountID, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE, businessDateUtil.getCurrentBusinessDate()));
        }


        // Email Event
        if(!productType.equals(ProductType.INTERNAL.name())){
            applicationEventPublisher.publishEvent(EmailUtil.prepareEmailEvent(this, ApiConstants.OPEN_ACCOUNT_EMAIL_EVENT, accountID, customer.get().getCustomerEmail(), customer.get().getCustomerName(),
                    businessDateUtil.getCurrentBusinessDate(), BigDecimal.ZERO, BigDecimal.ZERO));
        }

        return buildAccountRsDTO(accountRepo.save(account));
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
    @Transactional(rollbackFor = {Exception.class})
    public AccountRsDTO closeAccount(AccountClosureRqDTO accountClosureRqDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(accountClosureRqDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(account.getIsClosed())
            throw new BadRequestException("Account Already Closed");

        Optional<Account> payAwayAccount = accountRepo.findById(accountClosureRqDTO.getPayAwayAccount());
        if(payAwayAccount.isEmpty())
            throw new AccountNotFoundException("Pay Away Account is Invalid");
        if(!payAwayAccount.get().getSubProduct().getProduct().getProductType().equals(ProductType.INTERNAL.name()))
            throw new BadRequestException("Pay Away Account should be an Internal Account");
        String productType = account.getSubProduct().getProduct().getProductType();

        if(!productType.equals(ProductType.CA.name()) && !productType.equals(ProductType.SA.name()))
            throw new BadRequestException("Only CASA Accounts can be closed");

        List<LoanDetails> loanDetailsList = loanDetailsRepo.findAllBySettlementAccount(account);
        if(!loanDetailsList.isEmpty())
            throw new BadRequestException("Cannot close the account linked as Settlement Account");

        List<FixedDepositDetails> fundingAccountList = fixtureRepo.findAllByFundingAccount(account);
        List<FixedDepositDetails> payAwayAccountsList = fixtureRepo.findAllByPayAwayAccount(account);

        if(!fundingAccountList.isEmpty() || !payAwayAccountsList.isEmpty())
            throw new BadRequestException("Cannot close the account linked as Funding/Pay Away Account");

        // Update Account Blocked Balance
        account.setBlockedBalance(BigDecimal.ZERO);
        accountRepo.save(account);

        // Credit Interest Application
        if(productType.equals(ProductType.SA.name()) || productType.equals(ProductType.CA.name())) {
            InterestApplicationRqDTO creditInterestApplicationRqDTO = InterestApplicationRqDTO.builder()
                    .accountID(account.getAccountID())
                    .interestApplicationType('C')
                    .build();
            transactionService.processInterestApplication(creditInterestApplicationRqDTO);
        }

        // Debit Interest Application
        if(productType.equals(ProductType.CA.name())) {
            InterestApplicationRqDTO debitInterestApplicationRqDTO = InterestApplicationRqDTO.builder()
                    .accountID(account.getAccountID())
                    .interestApplicationType('D')
                    .build();
            transactionService.processInterestApplication(debitInterestApplicationRqDTO);
        }

        // Pay away Transaction
        account = accountRepo.findById(accountClosureRqDTO.getAccountID()).get();
        List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();

        if(account.getClearedBalance().compareTo(BigDecimal.ZERO) > 0) {
            transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getAccountID(), account.getClearedBalance().negate(), 'D'));
            transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(payAwayAccount.get().getAccountID(), account.getClearedBalance(), 'C'));
        } else {
            transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(payAwayAccount.get().getAccountID(), account.getClearedBalance(), 'D'));
            transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(account.getAccountID(), account.getClearedBalance().negate(), 'C'));
        }

        TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                .transactionDetails(transactionDetailsRqDTOList)
                .build();
        transactionService.processTransaction(transactionRqDTO, TransactionCode.A00);


        // Remove Lending Blocks
        List<LendingBlocks> lendingBlocks = lendingBlocksRepo.findBySettlementAccountID(account.getAccountID());
        for(LendingBlocks lendingBlock : lendingBlocks){
            lendingBlocksRepo.delete(lendingBlock);
        }

        // Update Account Details
        account = accountRepo.findById(accountClosureRqDTO.getAccountID()).get();
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
    public InterestEnquiryRsDTO getInterestDetails(String accountID) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(accountID);
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");

        Account account = accountOptional.get();
        if(!InterestUtils.isInterestAccrualProduct(account.getSubProduct())) {
            throw new BadRequestException("Account does not have an Interest Accrual Feature");
        }
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

    public String generateAccountID(Customer customer){
        long count = accountRepo.countByCustomer(customer);
        return ApiConstants.ACCID_PREFIX+customer.getCustomerID()+(count+1);
    }

    public String generateAccountID(Branch branch){
        long count = accountRepo.countByBranchAndCustomer(branch, null);
        return ApiConstants.ACCID_PREFIX+branch.getBranchCode()+(count+1);
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
                .customer(account.getCustomer() == null ? null : customerService.buildCustomerRsDTO(account.getCustomer()))
                .subProduct(subProductService.buildSubProductRsDTO(account.getSubProduct()))
                .closureReason(account.getClosureReason())
                .build();
    }
}
