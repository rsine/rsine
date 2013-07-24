package eu.lod2.rsine.querydispatcher;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;

import java.util.Iterator;

public class QueryDispatcher implements IQueryDispatcher {

    private RegistrationService registrationService;
    private ChangeSetStore changeSetStore;

    @Override
    public void trigger() {
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();
        while (subscriptionIt.hasNext()) {

        }
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

}
