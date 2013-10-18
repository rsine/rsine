package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.registrationservice.NotificationQuery;

import java.util.Date;

public class MinTimePassedEvaluationPolicy implements IEvaluationPolicy {

    private long minSecondsBetweenEvaluations;

    public MinTimePassedEvaluationPolicy(long minSecondsBetweenEvaluations) {
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
