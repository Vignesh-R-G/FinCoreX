package com.bankfusion.corebanking.service;

import com.bankfusion.corebanking.dto.BusinessDateDTO;

import java.sql.Date;

public interface BusinessDateService {
    public BusinessDateDTO getBusinessDate();
    public String updateBusinessDate(Date businessDate);
}
