package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.*;
import com.fincorex.corebanking.exception.*;

import java.util.List;

public interface AccountService {
    public AccountRsDTO openAccount(OpenAccountRqDTO openAccountRqDTO, Boolean isInvokedFromLoanService) throws BranchNotFoundException, CustomerNotFoundException, SubProductNotFoundException, BadRequestException;
    public AccountRsDTO findAccountByID(String accountID) throws AccountNotFoundException;
    public List<AccountRsDTO> findAccountsByCustomer(String customerID, int pageNumber, int pageSize) throws CustomerNotFoundException;
    public List<AccountRsDTO> findAccountsBySubProduct(String subProductID, int pageNumber, int pageSize) throws SubProductNotFoundException;
    public AccountRsDTO closeAccount(AccountClosureRqDTO accountClosureRqDTO) throws AccountNotFoundException, BadRequestException;
    public BalanceEnquiryRsDTO getAccountBalance(String accountID) throws AccountNotFoundException;
    public InterestEnquiryRsDTO getInterestDetails(String accountID) throws AccountNotFoundException, BadRequestException;

}
