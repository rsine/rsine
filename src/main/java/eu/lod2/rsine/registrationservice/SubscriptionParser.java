package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import org.openrdf.model.Model;

import java.util.Collection;
import java.util.Collections;

public class SubscriptionParser {

    private Model rdfSubscription;

    public SubscriptionParser(Model rdfSubscription) {
        this.rdfSubscription = rdfSubscription;
    }

    public Subscription createSubscription() {
        Subscription subscription = new Subscription();

        for (INotifier notifier : createNotifiers()) {
            subscription.addNotifier(notifier);
        }

        for (String query : getSparqlQueries()) {
            subscription.addQuery(query);
        }

        return subscription;
    }

    private Collection<INotifier> createNotifiers() {
        return Collections.EMPTY_LIST;
    }

    private Collection<String> getSparqlQueries() {
        return Collections.EMPTY_LIST;
    }

}
