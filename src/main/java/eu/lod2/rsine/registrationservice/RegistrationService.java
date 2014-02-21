package eu.lod2.rsine.registrationservice;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

@Component
public class RegistrationService {

    private final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private Collection<Subscription> subscriptions = new HashSet<Subscription>();

    public RegistrationService() {
    }

    public synchronized void register(Subscription subscription, boolean deleteOthers) {
        if (deleteOthers) {
            subscriptions.clear();
        }
        subscriptions.add(subscription);
    }

    public synchronized Resource register(Model subscriptionData) {
        return register(subscriptionData, false);
    }

    public synchronized Resource register(Model subscriptionData, boolean overwriteIfExisting) {
        Subscription subscription = new SubscriptionParser(subscriptionData).createSubscription();

        if (subscriptions.contains(subscription)) {
            if (overwriteIfExisting) {
                subscriptions.remove(subscription);
            }
            else throw new SubscriptionExistsException("Subscription already registered");
        }

        subscriptions.add(subscription);
        logger.info("Successfully registered subscription " +subscription.getId());
        return subscription.getId();
    }

    public synchronized void unregister(Resource subscriptionId) {
        subscriptions.remove(getSubscription(subscriptionId));
    }

    public synchronized Iterator<Subscription> getSubscriptionIterator() {
        return subscriptions.iterator();
    }

    public synchronized Subscription getSubscription(Resource subscriptionId) {
        for (Subscription subscription : subscriptions) {
            if (subscription.getId().equals(subscriptionId)) return subscription;
        }
        throw new SubscriptionNotFoundException();
    }

}
