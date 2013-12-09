package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.queryhandling.EvaluationPostponedException;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MinTimePassedEvaluationPolicy implements IEvaluationPolicy {

    private final Logger logger = LoggerFactory.getLogger(MinTimePassedEvaluationPolicy.class);

    private long minMillisBetweenEvaluations;
    private Map<NotificationQuery, Long> timeBetweenEvaluations = new HashMap<NotificationQuery, Long>();

    public MinTimePassedEvaluationPolicy(long minMillisBetweenEvaluations) {
        logger.info("Minimum milliseconds between evaluation: " +minMillisBetweenEvaluations);
        this.minMillisBetweenEvaluations = minMillisBetweenEvaluations;
    }

    @Override
    public void checkEvaluationNeeded(NotificationQuery query) {
        Date queryLastIssued = query.getLastIssued();
        if (queryLastIssued == null) return;

        long millisPassed = new Date().getTime() - queryLastIssued.getTime();
        if (millisPassed <= getMinTimeForQuery(query)) {
            throw new EvaluationPostponedException();
        }
    }

    private long getMinTimeForQuery(NotificationQuery query) {
        Long time = timeBetweenEvaluations.get(query);
        if (time == null) {
            time = minMillisBetweenEvaluations + Math.round(Math.random() * 3 * minMillisBetweenEvaluations);
            timeBetweenEvaluations.put(query, time);
        }
        return time;
    }

}
