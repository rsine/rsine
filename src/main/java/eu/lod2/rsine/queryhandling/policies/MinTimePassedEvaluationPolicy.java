package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.queryhandling.EvaluationPostponedException;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MinTimePassedEvaluationPolicy implements IEvaluationPolicy {

    private final Logger logger = LoggerFactory.getLogger(MinTimePassedEvaluationPolicy.class);
    private long minMillisBetweenEvaluations;

    public MinTimePassedEvaluationPolicy(long minMillisBetweenEvaluations) {
        logger.info("Minimum milliseconds between evaluation: " +minMillisBetweenEvaluations);
        this.minMillisBetweenEvaluations = minMillisBetweenEvaluations;
    }

    @Override
    public void checkEvaluate(NotificationQuery query) {
        Date queryLastIssued = query.getLastIssued();
        if (queryLastIssued == null) return;

        long millisPassed = new Date().getTime() - queryLastIssued.getTime();
        if (millisPassed <= minMillisBetweenEvaluations) {
            throw new EvaluationPostponedException();
        }
    }

}
