package com.fincorex.corebanking.batch.interestaccrual;

import com.fincorex.corebanking.entity.InterestAccrualTag;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class InterestAccrualItemWriter implements ItemWriter<InterestAccrualTag> {

    public void write(Chunk<? extends InterestAccrualTag> interestAccrualRecords) throws Exception{
        interestAccrualRecords.forEach(interestAccrualRecord->{
        });
    }
}

