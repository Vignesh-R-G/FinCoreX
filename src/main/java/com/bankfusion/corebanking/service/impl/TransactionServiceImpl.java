package com.bankfusion.corebanking.service.impl;

import com.bankfusion.corebanking.constants.ApiConstants;
import com.bankfusion.corebanking.dto.*;
import com.bankfusion.corebanking.entity.Account;
import com.bankfusion.corebanking.entity.TransactionDetails;
import com.bankfusion.corebanking.enums.InterestMethod;
import com.bankfusion.corebanking.enums.ProductType;
import com.bankfusion.corebanking.enums.TransactionCode;
import com.bankfusion.corebanking.exception.AccountNotFoundException;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.TransactionNotFoundException;
import com.bankfusion.corebanking.repository.AccountRepo;
import com.bankfusion.corebanking.repository.TransactionRepo;
import com.bankfusion.corebanking.service.TransactionService;
import com.bankfusion.corebanking.utils.BusinessDateUtil;
import com.bankfusion.corebanking.utils.InterestUtils;
import com.bankfusion.corebanking.utils.TransactionUtils;
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
    @Transactional
    public TransactionRsDTO processCASATransaction(TransactionRqDTO transactionRqDTO) throws BadRequestException, AccountNotFoundException {
        transactionUtils.validateTransactionRequest(transactionRqDTO);
        Date businessDate = businessDateUtil.getCurrentBusinessDate();
        List<TransactionDetailsRsDTO> transactionDetailsRs = new ArrayList<>();
        String transactionID = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String transactionCode = TransactionCode.A00.name();
        String narration = TransactionCode.A00.getNarration();
        for(TransactionDetailsRqDTO transactionDetailDTO : transactionRqDTO.getTransactionDetails()) {
            Account account = new Account();
            account.setClearedBalance(transactionDetailDTO.getDebitCreditFlag() == 'D' ?
                    account.getClearedBalance().subtract(transactionDetailDTO.getAmount())
                    : account.getClearedBalance().add(transactionDetailDTO.getAmount()));
            account.setLastAccrualDate(businessDate);
            accountRepo.save(account);
            TransactionDetails transactionDetails = TransactionDetails.builder()
                    .transactionID(transactionID)
                    .transactionCode(transactionCode)
                    .transactionDate(businessDate)
                    .accountID(transactionDetailDTO.getAccountID())
                    .amount(transactionDetailDTO.getAmount())
                    .narration(narration)
                    .debitCreditFlag(transactionDetailDTO.getDebitCreditFlag())
                    .build();
            transactionDetailsRs.add(buildTransactionDetailsRsDTO(transactionRepo.save(transactionDetails)));

            if(InterestUtils.isInterestAccrualProduct(account.getSubProduct())) {
                applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, transactionDetailDTO.getAccountID(), transactionDetailDTO.getAmount(), BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE));
            }
        }
        return buildTransactionRsDTO(transactionDetailsRs, transactionID, transactionCode, narration);
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
    public UnBlockTransactionDTO unBlockAmount(UnBlockTransactionDTO unBlockTransactionDTO) throws AccountNotFoundException, BadRequestException {
        Optional<Account> accountOptional = accountRepo.findById(unBlockTransactionDTO.getAccountID());
        if(accountOptional.isEmpty())
            throw new AccountNotFoundException("Account Not Found");
        Account account = accountOptional.get();
        if(account.getBlockedBalance().compareTo(unBlockTransactionDTO.getUnBlockAmount())<0)
            throw new BadRequestException("UnBlock Amount should be greater than the available block amount");
        account.setBlockedBalance(account.getBlockedBalance().subtract(unBlockTransactionDTO.getUnBlockAmount()));
        accountRepo.save(account);
        return unBlockTransactionDTO;
    }

    @Override
    @Transactional
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

        BigDecimal transactionAmount = interestApplicationRqDTO.getInterestApplicationType() == 'C' ? account.getCreditAccruedInterest()
                : account.getDebitAccruedInterest().negate();

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
        processCASATransaction(transactionRqDTO);

        // Interest Accrual Changes Events Due to Application
        if(interestApplicationRqDTO.getInterestApplicationType() == 'C') {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, interestApplicationRqDTO.getAccountID(), BigDecimal.ZERO, BigDecimal.ZERO,
                    transactionAmount.abs(), BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE));
        } else {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_TRANSACTION_EVENT, interestApplicationRqDTO.getAccountID(), BigDecimal.ZERO, transactionAmount.abs(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE, Boolean.FALSE));
        }

        return "Interest Application is successful for the accountID : "+interestApplicationRqDTO.getAccountID();
    }

    @Override
    @Transactional
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
        if(interestRateType == 'C') {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_RATE_CHANGE_EVENT, accountID, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, interestRateChangeRqDTO.getInterestRate(), Boolean.FALSE, Boolean.TRUE));
        } else {
            applicationEventPublisher.publishEvent(InterestUtils.prepareInterestHistoryEvent(this, ApiConstants.INT_HISTORY_RATE_CHANGE_EVENT, accountID, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, interestRateChangeRqDTO.getInterestRate(), BigDecimal.ZERO, Boolean.TRUE, Boolean.FALSE));
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
