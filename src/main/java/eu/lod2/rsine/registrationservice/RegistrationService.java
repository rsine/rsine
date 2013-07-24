package eu.lod2.rsine.registrationservice;

import eu.lod2.util.ItemNotFoundException;
import org.openrdf.model.URI;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class RegistrationService {

    private Collection<Subscription> subscriptions = new HashSet<Subscription>();

    public Subscription requestSubscription() {
        return new Subscription();
    }

    public void register(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public Subscription getSubscription(URI subscriber) {
        for (Subscription subscription : subscriptions) {
            if (subscription.getSubscriber().equals(subscriber)) return subscription;
        }
        throw new ItemNotFoundException("No subscription of subscriber '" +subscriber.stringValue()+ "' found");
    }

    public Iterator<Subscription> getSubscriptionIterator() {
        return subscriptions.iterator();
    }

}
