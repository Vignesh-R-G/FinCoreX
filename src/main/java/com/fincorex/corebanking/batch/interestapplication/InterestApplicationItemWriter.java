package com.fincorex.corebanking.batch.interestapplication;

import com.fincorex.corebanking.entity.InterestApplicationTag;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class InterestApplicationItemWriter implements ItemWriter<InterestApplicationTag> {

    public void write(Chunk<? extends InterestApplicationTag> interestApplicationRecords) throws Exception{
        interestApplicationRecords.forEach(interestApplicationRecord->{
        });
    }
}

