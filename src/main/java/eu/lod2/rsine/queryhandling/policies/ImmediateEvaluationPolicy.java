package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.springframework.stereotype.Component;

@Component
public class ImmediateEvaluationPolicy implements IEvaluationPolicy {

    @Override
    public boolean shouldEvaluate(NotificationQuery query) {
        return true;
    }

}
