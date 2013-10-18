package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.registrationservice.NotificationQuery;

public interface IEvaluationPolicy {

    boolean shouldEvaluate(NotificationQuery query);

}
