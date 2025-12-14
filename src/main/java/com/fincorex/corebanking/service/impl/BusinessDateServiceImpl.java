package com.fincorex.corebanking.service.impl;

import com.fincorex.corebanking.dto.BusinessDateDTO;
import com.fincorex.corebanking.service.BusinessDateService;
import com.fincorex.corebanking.utils.BusinessDateUtil;
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
        businessDateUtil.updateBusinessDate(businessDate);
        return "Business Date Updated";
    }
}
