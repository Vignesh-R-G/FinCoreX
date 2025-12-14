package com.fincorex.corebanking.batch.repaymentcollection;

import com.fincorex.corebanking.entity.RepaymentCollectionTag;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class RepaymentCollectionItemWriter implements ItemWriter<RepaymentCollectionTag> {

    public void write(Chunk<? extends RepaymentCollectionTag> repaymentRecords) throws Exception{
        repaymentRecords.forEach(repaymentRecord->{
        });
    }
}

