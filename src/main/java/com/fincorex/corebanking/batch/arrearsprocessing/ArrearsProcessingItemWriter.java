package com.fincorex.corebanking.batch.arrearsprocessing;

import com.fincorex.corebanking.entity.ArrearsProcessingTag;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ArrearsProcessingItemWriter implements ItemWriter<ArrearsProcessingTag> {

    public void write(Chunk<? extends ArrearsProcessingTag> arrearsProcessingRecords) throws Exception{
        arrearsProcessingRecords.forEach(arrearsProcessingRecord->{
        });
    }
}

