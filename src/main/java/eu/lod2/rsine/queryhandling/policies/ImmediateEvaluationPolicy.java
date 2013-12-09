package eu.lod2.rsine.queryhandling.policies;

import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.springframework.stereotype.Component;

@Component
public class ImmediateEvaluationPolicy implements IEvaluationPolicy {

    @Override
    public void checkEvaluationNeeded(NotificationQuery query) {
        // always allow, i.e. don't throw any exception
    }

}
