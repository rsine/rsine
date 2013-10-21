package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MinTimePassedEvaluationPolicy implements IEvaluationPolicy {

    private final Logger logger = LoggerFactory.getLogger(MinTimePassedEvaluationPolicy.class);
    private long minSecondsBetweenEvaluations;

    public MinTimePassedEvaluationPolicy(long minSecondsBetweenEvaluations) {
        logger.info("Minimum seconds between evaluation: " +minSecondsBetweenEvaluations);
        this.minSecondsBetweenEvaluations = minSecondsBetweenEvaluations;
    }

    @Override
    public boolean shouldEvaluate(NotificationQuery query) {
        Date queryLastIssued = query.getLastIssued();
        if (queryLastIssued == null) return true;

        long secsPassed = Math.round((new Date().getTime() - queryLastIssued.getTime()) / 1000);
        return secsPassed > minSecondsBetweenEvaluations;
    }

}
