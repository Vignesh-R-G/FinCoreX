package com.fincorex.corebanking.utils;

import com.fincorex.corebanking.constants.ApiConstants;
import com.fincorex.corebanking.entity.Property;
import com.fincorex.corebanking.repository.PropertyRepo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class BusinessDateUtil {
    private Date businessDate;

    @Autowired
    private PropertyRepo propertyRepo;


    @Scheduled(cron = "0 0 0 * * *")
    public void updateBusinessDate(){
        businessDate = Date.valueOf(businessDate.toLocalDate().plusDays(1));
    }

    public Date getCurrentBusinessDate(){
        return businessDate;
    }

    public synchronized void updateBusinessDate(Date requestedDate){
        businessDate = requestedDate;
    }

    @PostConstruct
    public void initializeBusinessDate(){
        // Fetch From Database Property if it is Present
        Optional<Property> businessDateProperty = propertyRepo.findById(ApiConstants.BUSINESS_DATE);
        if(businessDateProperty.isPresent()){
            businessDate = Date.valueOf(businessDateProperty.get().getValue());
        } else {
            businessDate = Date.valueOf(LocalDate.now());
        }
    }

    @PreDestroy
    public void persistBusinessDateProperty(){
        // Updating to Database Property
        Optional<Property> businessDateProperty = propertyRepo.findById(ApiConstants.BUSINESS_DATE);
        if(businessDateProperty.isPresent()){
            businessDateProperty.get().setValue(businessDate.toString());
            propertyRepo.save(businessDateProperty.get());
        } else{
            Property property = Property.builder()
                    .key("BUSINESS_DATE")
                    .value(businessDate.toString())
                    .build();
            propertyRepo.save(property);
        }
    }
}
