package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.FixedDepositDetails;
import com.fincorex.corebanking.enums.FixtureStatus;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.*;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.FixtureRepo;
import com.fincorex.corebanking.service.AccountService;
import com.fincorex.corebanking.service.FixtureService;
import com.fincorex.corebanking.service.TransactionService;
import com.fincorex.corebanking.utils.BusinessDateUtil;
import com.fincorex.corebanking.utils.InterestUtils;
import com.fincorex.corebanking.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FixtureServiceImpl implements FixtureService {
    @Autowired
    private FixtureRepo fixtureRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BusinessDateUtil businessDateUtil;

    @Autowired
    private TransactionUtils transactionUtils;

    @Autowired
    private TransactionService transactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FixtureRsDTO openFixedDeposit(FixtureRqDTO fixtureRqDTO) throws AccountNotFoundException, SubProductNotFoundException, BranchNotFoundException, BadRequestException, CustomerNotFoundException {
        Optional<Account> fundingAccount = accountRepo.findById(fixtureRqDTO.getFundingAccount());
        if(fundingAccount.isEmpty())
            throw new AccountNotFoundException("Funding Account Not Found");

        String fundingAccProductType = fundingAccount.get().getSubProduct().getProduct().getProductType();
        if(!fundingAccProductType.equals(ProductType.CA.name()) && !fundingAccProductType.equals(ProductType.SA.name())){
            throw new BadRequestException("Funding Account should be a CASA Account");
        }

        Optional<Account> payAwayAccount = accountRepo.findById(fixtureRqDTO.getPayAwayAccount());
        if(payAwayAccount.isEmpty())
            throw new AccountNotFoundException("Pay Away Account Not Found");
        String payAwayAccProductType = payAwayAccount.get().getSubProduct().getProduct().getProductType();
        if(!payAwayAccProductType.equals(ProductType.CA.name()) && !payAwayAccProductType.equals(ProductType.SA.name())){
            throw new BadRequestException("Pay Away Account should be a CASA Account");
        }

        BigDecimal fundingAccountAvailableBalance = fundingAccount.get().getClearedBalance().subtract(fundingAccount.get().getBlockedBalance());
        if(fundingAccountAvailableBalance.compareTo(fixtureRqDTO.getFixtureAmount()) < 0)
            throw new BadRequestException("Funding Account does not have sufficient balance");

        AccountRsDTO accountRsDTO =  accountService.openAccount(fixtureRqDTO.getOpenAccountRqDTO(), Boolean.FALSE, Boolean.TRUE);

        Account fixtureAccount = accountRepo.findById(accountRsDTO.getAccountID()).get();

        FixedDepositDetails fixedDepositDetails = FixedDepositDetails.builder()
                .fixtureAccountID(accountRsDTO.getAccountID())
                .fixtureStatus(FixtureStatus.NORMAL.name())
                .term(fixtureRqDTO.getTerm())
                .fixtureStartDate(businessDateUtil.getCurrentBusinessDate())
                .maturityDate(calculateMaturityDate(businessDateUtil.getCurrentBusinessDate(), fixtureRqDTO.getTerm()))
                .account(fixtureAccount)
                .fundingAccount(fundingAccount.get())
                .payAwayAccount(payAwayAccount.get())
                .fixtureAmount(fixtureRqDTO.getFixtureAmount())
                .build();
        fixedDepositDetails = fixtureRepo.save(fixedDepositDetails);

        processFixtureDepositTransaction(fixedDepositDetails);

        return buildFixtureRsDTO(fixedDepositDetails);
    }

    @Override
    public FixtureRsDTO fetchFixedDepositDetails(String accountID) throws AccountNotFoundException {
        Optional<FixedDepositDetails> fixedDepositDetails = fixtureRepo.findById(accountID);
        if(fixedDepositDetails.isEmpty())
            throw new AccountNotFoundException("Fixture Account Not Found");
        return buildFixtureRsDTO(fixedDepositDetails.get());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processFixtureBreakage(String accountID) throws AccountNotFoundException, BadRequestException {
        Optional<FixedDepositDetails> fixedDepositDetails = fixtureRepo.findById(accountID);
        if(fixedDepositDetails.isEmpty())
            throw new AccountNotFoundException("Invalid FD Account");
        if(fixedDepositDetails.get().getFixtureStatus().equals(FixtureStatus.BROKEN.name()) || fixedDepositDetails.get().getFixtureStatus().equals(FixtureStatus.MATURED.name()))
            throw new BadRequestException("Fixed Deposit is already matured or broken");

        processBreakageOrMaturityTransaction(fixedDepositDetails.get());

        // Update Fixture Status
        fixedDepositDetails.get().setFixtureStatus(FixtureStatus.BROKEN.name());
        fixtureRepo.save(fixedDepositDetails.get());

        return "Breakage Successful for the account id : "+accountID;
    }

    private void processFixtureDepositTransaction(FixedDepositDetails fixedDepositDetails) throws BadRequestException, AccountNotFoundException {
        List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(fixedDepositDetails.getFundingAccount().getAccountID(), fixedDepositDetails.getFixtureAmount().negate(), 'D'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(fixedDepositDetails.getFixtureAccountID(), fixedDepositDetails.getFixtureAmount(), 'C'));

        TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                .transactionDetails(transactionDetailsRqDTOList)
                .build();
        transactionService.processTransaction(transactionRqDTO, TransactionCode.FD0);
    }

    public void processBreakageOrMaturityTransaction(FixedDepositDetails fixedDepositDetails) throws BadRequestException, AccountNotFoundException {
        // Interest Application
        InterestApplicationRqDTO creditInterestApplicationRqDTO = InterestApplicationRqDTO.builder()
                .accountID(fixedDepositDetails.getFixtureAccountID())
                .interestApplicationType('C')
                .build();
        transactionService.processInterestApplication(creditInterestApplicationRqDTO);

        // Pay Away Transactions
        List<TransactionDetailsRqDTO> transactionDetailsRqDTOList = new ArrayList<>();
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(fixedDepositDetails.getFixtureAccountID(), fixedDepositDetails.getAccount().getClearedBalance().negate(), 'D'));
        transactionDetailsRqDTOList.add(transactionUtils.prepareTransactionDetailsRequest(fixedDepositDetails.getPayAwayAccount().getAccountID(), fixedDepositDetails.getAccount().getClearedBalance(), 'C'));

        TransactionRqDTO transactionRqDTO = TransactionRqDTO.builder()
                .transactionDetails(transactionDetailsRqDTOList)
                .build();
        transactionService.processTransaction(transactionRqDTO, TransactionCode.FD1);
    }

    public FixtureRsDTO buildFixtureRsDTO(FixedDepositDetails fixedDepositDetails){
        Date lastInterestAccrualDate = fixedDepositDetails.getAccount().getLastAccrualDate();
        if(lastInterestAccrualDate == null)
            lastInterestAccrualDate = businessDateUtil.getCurrentBusinessDate();

        BigDecimal interestAtMaturity = InterestUtils.calculateAccruedInterest(fixedDepositDetails.getAccount().getClearedBalance(), fixedDepositDetails.getAccount().getCreditInterestRate(),
                fixedDepositDetails.getAccount().getCreditAccruedInterest(), lastInterestAccrualDate, fixedDepositDetails.getMaturityDate());
        return FixtureRsDTO.builder()
                .accountID(fixedDepositDetails.getFixtureAccountID())
                .fixtureAmount(fixedDepositDetails.getFixtureAmount())
                .fixtureStatus(fixedDepositDetails.getFixtureStatus())
                .totalAmountAtMaturity(fixedDepositDetails.getAccount().getClearedBalance().add(interestAtMaturity))
                .totalTerm(fixedDepositDetails.getTerm())
                .fixtureStartDate(fixedDepositDetails.getFixtureStartDate())
                .maturityDate(fixedDepositDetails.getMaturityDate())
                .build();
    }

    private Date calculateMaturityDate(Date fixtureStartDate, Long term){
        return Date.valueOf(fixtureStartDate.toLocalDate().plusMonths(term));
    }
}
