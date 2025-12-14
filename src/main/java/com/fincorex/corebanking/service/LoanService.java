package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.LoanAccountRsDTO;
import com.fincorex.corebanking.dto.LoanEstablishmentRqDTO;
import com.fincorex.corebanking.exception.*;

public interface LoanService {
    public LoanAccountRsDTO establishLoan (LoanEstablishmentRqDTO loanEstablishmentRqDTO) throws SubProductNotFoundException, BranchNotFoundException, CustomerNotFoundException, BadRequestException, DelinquencyProfileNotFoundException, AccountNotFoundException;
    public LoanAccountRsDTO fetchLoanDetails (String loanAccountID) throws BadRequestException;
}
