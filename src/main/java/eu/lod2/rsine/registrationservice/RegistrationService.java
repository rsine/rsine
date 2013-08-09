package eu.lod2.rsine.registrationservice;

import org.openrdf.model.Model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class RegistrationService {

    private Collection<Subscription> subscriptions = new HashSet<Subscription>();

    public void register(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void register(Model subscription) {
        subscriptions.add(new SubscriptionParser(subscription).createSubscription());
    }

    public Iterator<Subscription> getSubscriptionIterator() {
        return subscriptions.iterator();
    }

}
