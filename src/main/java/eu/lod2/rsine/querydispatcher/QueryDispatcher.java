package eu.lod2.rsine.querydispatcher;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class QueryDispatcher implements IQueryDispatcher {

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);

    private RegistrationService registrationService;
    private ChangeSetStore changeSetStore;

    @Override
    public void trigger() {
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();
        while (subscriptionIt.hasNext()) {
            Subscription subscription = subscriptionIt.next();
            logger.info("Dispatching queries for user '" +subscription.getSubscriber()+ "'");
        }
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

}
