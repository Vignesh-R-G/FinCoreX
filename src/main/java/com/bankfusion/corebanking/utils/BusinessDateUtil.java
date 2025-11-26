package com.bankfusion.corebanking.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class BusinessDateUtil {
    private Date businessDate;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateBusinessDate(){
        businessDate = Date.valueOf(businessDate.toLocalDate().plusDays(1));
    }

    public Date getCurrentBusinessDate(){
        return businessDate;
    }

    public void updateBusinessDate(Date requestedDate){
        businessDate = requestedDate;
    }

    @PostConstruct
    public void initializeBusinessDate(){
        businessDate = Date.valueOf(LocalDate.now());
    }
}
