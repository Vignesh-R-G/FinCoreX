package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.FixtureRqDTO;
import com.fincorex.corebanking.dto.FixtureRsDTO;
import com.fincorex.corebanking.exception.*;

public interface FixtureService {
    public FixtureRsDTO openFixedDeposit(FixtureRqDTO fixtureRqDTO) throws AccountNotFoundException, SubProductNotFoundException, BranchNotFoundException, BadRequestException, CustomerNotFoundException;
    public FixtureRsDTO fetchFixedDepositDetails(String accountID) throws AccountNotFoundException;
    public String processFixtureBreakage(String accountID) throws AccountNotFoundException, BadRequestException;
}
