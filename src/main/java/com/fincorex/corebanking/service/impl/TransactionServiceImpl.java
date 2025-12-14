package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.TransactionDetails;
import com.fincorex.corebanking.enums.InterestMethod;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.TransactionNotFoundException;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.TransactionRepo;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import com.fincorex.corebanking.utils.EmailUtil;
import com.fincorex.corebanking.utils.InterestUtils;
import com.fincorex.corebanking.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public TransactionRsDTO processTransaction(TransactionRqDTO transactionRqDTO, TransactionCode transactionCode) throws BadRequestException, AccountNotFoundException {
        transactionUtils.validateTransactionRequest(transactionRqDTO, transactionCode);
        Date businessDate = businessDateUtil.getCurrentBusinessDate();
        List<TransactionDetailsRsDTO> transactionDetailsRs = new ArrayList<>();
        String transactionID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String transCode = transactionCode.name();
        String narration = transactionCode.getNarration();
        for(TransactionDetailsRqDTO transactionDetailDTO : transactionRqDTO.getTransactionDetails()) {
            Account account = accountRepo.findById(transactionDetailDTO.getAccountID()).get();
            account.setClearedBalance(account.getClearedBalance().add(transactionDetailDTO.getAmount()));
            accountRepo.save(account);
            TransactionDetails transactionDetails = TransactionDetails.builder()
                    .transactionID(transactionID)
                    .transactionCode(transCode)
                    .transactionDate(businessDate)
                    .accountID(transactionDetailDTO.getAccountID())
                    .amount(transactionDetailDTO.getAmount())
                    .narration(narration)
                    .debitCreditFlag(transactionDetailDTO.getDebitCreditFlag())
                    .build();
            transactionDetailsRs.add(buildTransactionDetailsRsDTO(transactionRepo.save(transactionDetails)));

            if(InterestUtils.isInterestAccrualProduct(account.getSubProduct())) {
                applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, transactionDetailDTO.getAccountID(), transactionDetailDTO.getAmount(), BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE, businessDate));
            }

            // Email Event
            String productType = account.getSubProduct().getProduct().getProductType();
            if(productType.equals(ProductType.CA.name()) || productType.equals(ProductType.SA.name())){
                applicationEventPublisher.publishEvent(EmailUtil.prepareEmailEvent(this, ApiConstants.TRANSACTION_EMAIL_EVENT, account.getAccountID(), account.getCustomer().getCustomerEmail(), account.getCustomer().getCustomerName(),
                        businessDateUtil.getCurrentBusinessDate(), transactionDetailDTO.getAmount(), account.getClearedBalance().subtract(account.getBlockedBalance())));
            }
        }
        return buildTransactionRsDTO(transactionDetailsRs, transactionID, transCode, narration);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public TransactionRsDTO fetchTransactionByID(String transactionID) throws TransactionNotFoundException {
        List<TransactionDetails> transactions = transactionRepo.findAllByTransactionID(transactionID);
        if(transactions == null || transactions.isEmpty())
            throw new TransactionNotFoundException("No Transaction Found For the given Transaction ID");
        List<TransactionDetailsRsDTO> transactionDetailsRs = new ArrayList<>();

        for(TransactionDetails transactionDetails : transactions){
            transactionDetailsRs.add(buildTransactionDetailsRsDTO(transactionDetails));
        }
        return buildTransactionRsDTO(transactionDetailsRs, transactionID, transactions.get(0).getTransactionCode(), transactions.get(0).getNarration());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public BlockTransactionDTO blockAmount(BlockTransactionDTO blockTransactionDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(blockTransactionDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(!account.getSubProduct().getProduct().getProductType().equals(ProductType.SA.name()) &&
            !account.getSubProduct().getProduct().getProductType().equals(ProductType.CA.name()))
            throw new BadRequestException("Blocking is only supported for the CASA Accounts");
        account.setBlockedBalance(account.getBlockedBalance().add(blockTransactionDTO.getBlockAmount()));
        accountRepo.save(account);
        return blockTransactionDTO;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public UnBlockTransactionDTO unBlockAmount(UnBlockTransactionDTO unBlockTransactionDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(unBlockTransactionDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(account.getBlockedBalance().compareTo(unBlockTransactionDTO.getUnBlockAmount())<0)
            throw new BadRequestException("UnBlock Amount should not be greater than the available block amount");
        account.setBlockedBalance(account.getBlockedBalance().subtract(unBlockTransactionDTO.getUnBlockAmount()));
        accountRepo.save(account);
        return unBlockTransactionDTO;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public String processInterestApplication(InterestApplicationRqDTO interestApplicationRqDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(interestApplicationRqDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(account.getSubProduct().getProduct().getProductType().equals(ProductType.INTERNAL.name()))
            throw new BadRequestException("Interest Application is not applicable for Internal Accounts");
        if(account.getSubProduct().getProduct().getProductType().equals(ProductType.LENDING.name()))
            throw new BadRequestException("Interest Application is not applicable for Loan Accounts");
        if(account.getSubProduct().getProduct().getProductType().equals(ProductType.SA.name()) &&
            interestApplicationRqDTO.getInterestApplicationType() == 'D')
            throw new BadRequestException("Debit Interest Application is not applicable for Savings Accounts");

        BigDecimal creditAccruedInterest = InterestUtils.calculateAccruedInterest(account.getClearedBalance(), account.getCreditInterestRate(),
                account.getCreditAccruedInterest(), account.getLastAccrualDate(), businessDateUtil.getCurrentBusinessDate());
        BigDecimal debitAccruedInterest = InterestUtils.calculateAccruedInterest(account.getClearedBalance(), account.getDebitInterestRate(),
                account.getDebitAccruedInterest(), account.getLastAccrualDate(), businessDateUtil.getCurrentBusinessDate());

        BigDecimal transactionAmount = interestApplicationRqDTO.getInterestApplicationType() == 'C' ? creditAccruedInterest
                : debitAccruedInterest.negate();
        Date transactionDate = businessDateUtil.getCurrentBusinessDate();

        if(!transactionAmount.equals(BigDecimal.ZERO)) {
            // Interest Application Postings
            List<TransactionDetailsRqDTO> transactionDetailsRqDTO = new ArrayList<>();
            transactionDetailsRqDTO.add(TransactionDetailsRqDTO.builder()
                    .accountID(interestApplicationRqDTO.getAccountID())
                    .amount(transactionAmount)
                    .debitCreditFlag(interestApplicationRqDTO.getInterestApplicationType() == 'C' ? 'C' : 'D')
                    .build());
            transactionDetailsRqDTO.add(TransactionDetailsRqDTO.builder()
                    .accountID(account.getSubProduct().getGlAccount())
                    .amount(transactionAmount.negate())
                    .debitCreditFlag(interestApplicationRqDTO.getInterestApplicationType() == 'C' ? 'D' : 'C')
                    .build());
            TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                    .transactionDetails(transactionDetailsRqDTO)
                    .build();
            processTransaction(transactionRqDTO, TransactionCode.I00);

            // Interest Accrual Changes Events Due to Application
            if (interestApplicationRqDTO.getInterestApplicationType() == 'C') {
                applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, interestApplicationRqDTO.getAccountID(), BigDecimal.ZERO, BigDecimal.ZERO,
                        transactionAmount.abs(), BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE, transactionDate));
            } else {
                applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, interestApplicationRqDTO.getAccountID(), BigDecimal.ZERO, transactionAmount.abs(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE, transactionDate));
            }
        }

        return "Interest Application is successful for the accountID : "+interestApplicationRqDTO.getAccountID();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public String processInterestRateChange(InterestRateChangeRqDTO interestRateChangeRqDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> account = accountRepo.findById(interestRateChangeRqDTO.getAccountID());
        Character interestRateType = interestRateChangeRqDTO.getInterestRateType();
        if(interestRateType != 'C' && interestRateType != 'D')
            throw new BadRequestException("Interest Rate Type should be 'D' or 'C'");
        if(account.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        if(account.get().getSubProduct().getProduct().getProductType().equals(ProductType.INTERNAL.name()))
            throw new BadRequestException("Interest Rate Change is not applicable for Internal Accounts");
        if(account.get().getSubProduct().getProduct().getProductType().equals(ProductType.LENDING.name()) && interestRateType == 'C')
            throw new BadRequestException("Credit Interest Rate Change is not applicable for Loan Accounts");
        if(account.get().getSubProduct().getProduct().getProductType().equals(ProductType.SA.name()) && interestRateType == 'D')
            throw new BadRequestException("Debit Interest Rate Change is not applicable for Savings Accounts");
        if(account.get().getSubProduct().getInterestMethod().equals(InterestMethod.FIXED.name()))
            throw new BadRequestException("Interest Rate Change is not applicable for Fixed Interest Accounts");
        String accountID = interestRateChangeRqDTO.getAccountID();
        Date businessDate = businessDateUtil.getCurrentBusinessDate();

        if(interestRateType == 'C') {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_RATE_CHANGE_EVENT, accountID, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, interestRateChangeRqDTO.getInterestRate(), Boolean.FALSE, Boolean.TRUE, businessDate));
        } else {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_RATE_CHANGE_EVENT, accountID, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, interestRateChangeRqDTO.getInterestRate(), BigDecimal.ZERO, Boolean.TRUE, Boolean.FALSE, businessDate));
        }
        return "Interest Rate Change is successful for the accountID : "+interestRateChangeRqDTO.getAccountID();
    }


    public TransactionDetailsRsDTO buildTransactionDetailsRsDTO(TransactionDetails transactionDetails){
        return TransactionDetailsRsDTO.builder()
                .accountID(transactionDetails.getAccountID())
                .debitCreditFlag(transactionDetails.getDebitCreditFlag())
                .amount(transactionDetails.getAmount())
                .build();
    }

    public TransactionRsDTO buildTransactionRsDTO(List<TransactionDetailsRsDTO> transactionDetailsRs, String transactionID, String transactionCode, String narration){
        return TransactionRsDTO.builder()
                .transactionDetails(transactionDetailsRs)
                .transactionID(transactionID)
                .transactionCode(transactionCode)
                .narration(narration)
                .build();
    }

}
