package com.fincorex.corebanking.utils;

import com.fincorex.corebanking.dto.TransactionDetailsRqDTO;
import com.fincorex.corebanking.dto.TransactionRqDTO;
import com.fincorex.corebanking.entity.Account;
import com.fincorex.corebanking.entity.LoanDetails;
import com.fincorex.corebanking.enums.ProductType;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.repository.AccountRepo;
import com.fincorex.corebanking.repository.LoanDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionUtils {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private LoanDetailsRepo loanDetailsRepo;

    public void validateTransactionRequest(TransactionRqDTO transactionRqDTO, TransactionCode transactionCode) throws BadRequestException, AccountNotFoundException {
        validateTransactionAmount(transactionRqDTO);
        validateAccountIDAndDCFlag(transactionRqDTO, transactionCode);
    }

    public void validateTransactionAmount(TransactionRqDTO transactionRqDTO) throws BadRequestException {
        List<TransactionDetailsRqDTO> transactionDetails = transactionRqDTO.getTransactionDetails();
        if(transactionDetails == null || transactionDetails.isEmpty())
            throw new BadRequestException("No Transaction Legs Found");
        BigDecimal totalTransactionAmount = BigDecimal.ZERO;
        for(TransactionDetailsRqDTO transactionDetail : transactionDetails){
            totalTransactionAmount = totalTransactionAmount.add(transactionDetail.getAmount());
        }
        if(totalTransactionAmount.compareTo(BigDecimal.ZERO) != 0)
            throw new BadRequestException("Transaction Imbalance. Total Credit should be equal to the Total Debit");
    }

    public void validateAccountIDAndDCFlag(TransactionRqDTO transactionRqDTO, TransactionCode transactionCode) throws AccountNotFoundException, BadRequestException {
        List<TransactionDetailsRqDTO> transactionDetails = transactionRqDTO.getTransactionDetails();
        for(TransactionDetailsRqDTO transactionDetail : transactionDetails){
            String accountID = transactionDetail.getAccountID();
            Optional<Account> account = accountRepo.findById(accountID);
            Optional<LoanDetails> loanDetails = loanDetailsRepo.findById(accountID);
            if(account.isEmpty())
                throw new AccountNotFoundException("Account Not Found");
            if(transactionDetail.getDebitCreditFlag() != 'C' && transactionDetail.getDebitCreditFlag() != 'D')
                throw new BadRequestException("Debit Credit Flag should be 'D' or 'C'");
            if(loanDetails.isPresent() && !transactionCode.name().equals(TransactionCode.LD0.name()) && !transactionCode.name().equals(TransactionCode.RPO.name()))
                throw new BadRequestException("Only Loan Repayment and Disbursement Transaction is allowed on the Loan Account");

            String productType = account.get().getSubProduct().getProduct().getProductType();
            BigDecimal availableBalance = account.get().getClearedBalance().subtract(account.get().getBlockedBalance());
            if(productType.equals(ProductType.SA.name()) && transactionDetail.getDebitCreditFlag() == 'D' && transactionDetail.getAmount().compareTo(BigDecimal.ZERO) != 0 &&
                    transactionDetail.getAmount().abs().compareTo(availableBalance) > 0)
                throw new BadRequestException("Overdraw is not allowed on Savings Account");

            if(productType.equals(ProductType.FD.name()) && !transactionCode.name().equals(TransactionCode.FD0.name()) && !transactionCode.name().equals(TransactionCode.I00.name())
                    && !transactionCode.name().equals(TransactionCode.FD1.name()))
                throw new BadRequestException("Only Fixture Deposit and Fixture Withdrawal Transactions are allowed on FD Accounts");

            if(account.get().getIsClosed())
                throw new BadRequestException("Transaction Failed. Account : "+ transactionDetail.getAccountID()+" is closed");
        }
    }

    public TransactionDetailsRqDTO prepareTransactionDetailsRequest(String accountID, BigDecimal amount, Character debitCreditFlag){
        return TransactionDetailsRqDTO.builder()
                .accountID(accountID)
                .amount(amount)
                .debitCreditFlag(debitCreditFlag)
                .build();
    }
}
