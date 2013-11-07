package eu.lod2.rsine.queryhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QueryProfiler {

    private final Logger logger = LoggerFactory.getLogger(QueryProfiler.class);

    private long queryCount, processingTimeSum;

    public void log(String query, long processingTime) {
        logger.info("Query execution and message creation took " +processingTime+"ms");
        queryCount++;
        processingTimeSum += processingTime;
    }

    public int getMeanQueryProcessingTime() {
        return Math.round(processingTimeSum / queryCount);
    }

}
