package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.enums.TransactionCode;
import com.fincorex.corebanking.exception.AccountNotFoundException;
import com.fincorex.corebanking.exception.BadRequestException;
import com.fincorex.corebanking.exception.TransactionNotFoundException;

public interface TransactionService {
    public TransactionRsDTO processTransaction(TransactionRqDTO transactionRqDTO, TransactionCode transactionCode) throws BadRequestException, AccountNotFoundException;
    public TransactionRsDTO fetchTransactionByID(String transactionID) throws TransactionNotFoundException;
    public BlockTransactionDTO blockAmount(BlockTransactionDTO blockTransactionRqDTO) throws AccountNotFoundException, BadRequestException;
    public UnBlockTransactionDTO unBlockAmount(UnBlockTransactionDTO unBlockTransactionDTO) throws AccountNotFoundException, BadRequestException;
    public String processInterestApplication(InterestApplicationRqDTO interestApplicationRqDTO) throws AccountNotFoundException, BadRequestException;
    public String processInterestRateChange(InterestRateChangeRqDTO interestRateChangeDTO) throws AccountNotFoundException, BadRequestException;
}
