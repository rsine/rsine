package eu.lod2.rsine.registrationservice;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class RegistrationService {

    private Collection<Subscription> subscriptions = new HashSet<Subscription>();

    public void register(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void register(Model subscriptionData) {
        Subscription subscription = new SubscriptionParser(subscriptionData).createSubscription();

        if (subscriptions.contains(subscription)) {
            throw new SubscriptionExistsException();
        }

        subscriptions.add(subscription);
    }

    public void unregister(Resource subscriptionId) {
        subscriptions.remove(getSubscriptionById(subscriptionId));
    }

    private Subscription getSubscriptionById(Resource subscriptionId){
        for (Subscription subscription : subscriptions){
            if(subscription.getSubscriptionId().equals(subscriptionId)){
                return subscription;
            }
        }        
        throw new SubscriptionNotFoundException();
    }

    public Iterator<Subscription> getSubscriptionIterator() {
        return subscriptions.iterator();
    }

}
