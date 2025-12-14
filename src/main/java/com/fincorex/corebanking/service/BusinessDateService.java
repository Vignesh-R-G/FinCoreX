package com.fincorex.corebanking.service;

import com.fincorex.corebanking.dto.BusinessDateDTO;

import java.sql.Date;

public interface BusinessDateService {
    public BusinessDateDTO getBusinessDate();
    public String updateBusinessDate(Date businessDate);
}
