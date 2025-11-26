package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.*;
import com.bankfusion.corebanking.exception.AccountNotFoundException;
import com.bankfusion.corebanking.exception.BadRequestException;
import com.bankfusion.corebanking.exception.TransactionNotFoundException;

public interface TransactionService {
    public TransactionRsDTO processCASATransaction(TransactionRqDTO transactionRqDTO) throws BadRequestException, AccountNotFoundException;
    public TransactionRsDTO fetchTransactionByID(String transactionID) throws TransactionNotFoundException;
    public BlockTransactionDTO blockAmount(BlockTransactionDTO blockTransactionRqDTO) throws AccountNotFoundException, BadRequestException;
    public UnBlockTransactionDTO unBlockAmount(UnBlockTransactionDTO unBlockTransactionDTO) throws AccountNotFoundException, BadRequestException;
    public String processInterestApplication(InterestApplicationRqDTO interestApplicationRqDTO) throws AccountNotFoundException, BadRequestException;
    public String processInterestRateChange(InterestRateChangeRqDTO interestRateChangeDTO) throws AccountNotFoundException, BadRequestException;
}
