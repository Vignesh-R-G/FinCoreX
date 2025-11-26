package com.bankfusion.corebanking.service.impl;

import com.bankfusion.corebanking.dto.BusinessDateDTO;
import com.bankfusion.corebanking.service.BusinessDateService;
import com.bankfusion.corebanking.utils.BusinessDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class BusinessDateServiceImpl implements BusinessDateService {
    @Autowired
    private BusinessDateUtil businessDateUtil;

    @Override
    public BusinessDateDTO getBusinessDate() {
        return BusinessDateDTO.builder().businessDate(businessDateUtil.getCurrentBusinessDate()).build();
    }

    @Override
    public String updateBusinessDate(Date businessDate) {
        return "Business Date Updated";
    }
}
