package eu.lod2.rsine.registrationservice;

import org.openrdf.model.Model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class RegistrationService {

    private Collection<Subscription> subscriptions = new HashSet<Subscription>();

    public void register(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void register(Model subscription) {
        subscriptions.add(new SubscriptionParser(subscription).createSubscription());
    }

    public void unregister(URI subscription) throws NoSuchRegistrationError {
        Subscription s = this.getSubscriptionByURI(subscription);        
        if(s!=null){
            this.subscriptions.remove(s);
        } else {
            throw new NoSuchRegistrationError();
        }
    }
    private Subscription getSubscriptionByURI(URI subscription){
        for(Subscription s : this.subscriptions){
            if(s.getSubscriber().equals(subscription)){
                return s;
            }
        }        
        return null;
    }
    public Iterator<Subscription> getSubscriptionIterator() {
        return subscriptions.iterator();
    }

}
