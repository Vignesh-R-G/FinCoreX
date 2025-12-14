package com.fincorex.corebanking.batch.repaymentcollection;


import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RepaymentCollectionPartitioner implements Partitioner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = jdbcTemplate.queryForObject("SELECT MIN(row_sequence) FROM bankfusion.repaymentcollectiontag", Long.class);
        long max = jdbcTemplate.queryForObject("SELECT MAX(row_sequence) FROM bankfusion.repaymentcollectiontag", Long.class);

        long targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        long start = min;
        long end = start + targetSize - 1;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start);
            context.putLong("maxId", end);
            result.put("partition" + i, context);
            start += targetSize;
            end += targetSize;
        }

        return result;
    }
}
