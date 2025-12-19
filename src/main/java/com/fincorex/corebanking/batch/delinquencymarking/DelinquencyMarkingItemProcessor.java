package com.fincorex.corebanking.batch.delinquencymarking;

import com.fincorex.corebanking.entity.DelinquencyMarkingTag;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class DelinquencyMarkingItemProcessor implements ItemProcessor<DelinquencyMarkingTag, DelinquencyMarkingTag> {

    @Override
    public DelinquencyMarkingTag process(DelinquencyMarkingTag delinquencyMarkingRecord) throws InterruptedException {
        return delinquencyMarkingRecord;
    }
}
