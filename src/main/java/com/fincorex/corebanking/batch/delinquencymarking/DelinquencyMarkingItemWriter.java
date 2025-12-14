package com.fincorex.corebanking.batch.delinquencymarking;

import com.fincorex.corebanking.entity.DelinquencyMarkingTag;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class DelinquencyMarkingItemWriter implements ItemWriter<DelinquencyMarkingTag> {

    public void write(Chunk<? extends DelinquencyMarkingTag> delinquencyMarkingRecords) throws Exception{
        delinquencyMarkingRecords.forEach(delinquencyMarkingRecord->{
        });
    }
}

