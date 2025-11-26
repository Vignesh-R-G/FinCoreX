package com.bankfusion.corebanking.utils;

import com.bankfusion.corebanking.dto.TransactionDetailsRqDTO;
import com.bankfusion.corebanking.dto.TransactionRqDTO;
import com.bankfusion.corebanking.entity.Account;
import com.bankfusion.corebanking.exception.AccountNotFoundException;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.repository.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionUtils {

    @Autowired
    private AccountRepo accountRepo;

    public void validateTransactionRequest(TransactionRqDTO transactionRqDTO) throws BadRequestException, AccountNotFoundException {
        validateTransactionAmount(transactionRqDTO);
        validateAccountIDAndDCFlag(transactionRqDTO);
    }

    public void validateTransactionAmount(TransactionRqDTO transactionRqDTO) throws BadRequestException {
        List<TransactionDetailsRqDTO> transactionDetails = transactionRqDTO.getTransactionDetails();
        if(transactionDetails == null || transactionDetails.isEmpty())
            throw new BadRequestException("No Transaction Legs Found");
        BigDecimal totalTransactionAmount = BigDecimal.ZERO;
        for(TransactionDetailsRqDTO transactionDetail : transactionDetails){
            totalTransactionAmount = totalTransactionAmount.add(transactionDetail.getAmount());
        }
        if(!totalTransactionAmount.equals(BigDecimal.ZERO))
            throw new BadRequestException("Transaction Imbalance. Total Credit should be equal to the Total Debit");
    }

    public void validateAccountIDAndDCFlag(TransactionRqDTO transactionRqDTO) throws AccountNotFoundException, BadRequestException {
        List<TransactionDetailsRqDTO> transactionDetails = transactionRqDTO.getTransactionDetails();
        for(TransactionDetailsRqDTO transactionDetail : transactionDetails){
            String accountID = transactionDetail.getAccountID();
            Optional<Account> account = accountRepo.findById(accountID);
            if(account.isEmpty())
                throw new AccountNotFoundException("Account Not Found");
            if(transactionDetail.getDebitCreditFlag() != 'C' && transactionDetail.getDebitCreditFlag() != 'D')
                throw new BadRequestException("Debit Credit Flag should be 'D' or 'C'");
        }
    }
}
