package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.util.Namespaces;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Subscription {

    private static int id = 0;

    private Resource subscriptionId;
    private NotificationQuery query;
    private Collection<INotifier> notifiers;

    public Subscription() {
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        subscriptionId = valueFactory.createURI(
            Namespaces.RSINE_NAMESPACE.getName(),
            "subscriber_" +id);
        notifiers = new ArrayList<INotifier>();
        id++;
    }

    public Resource getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Resource subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public NotificationQuery getQuery() {
        return query;
    }

    public void setQuery(String query, BindingSetFormatter formatter) {
        new NotificationQuery(query, formatter, this);
    }

    public void setQuery(String query, BindingSetFormatter formatter, Condition condition) {
        new NotificationQuery(query, formatter, condition, this);
    }

    public void addNotifier(INotifier notifier) {
        notifiers.add(notifier);
    }

    public Iterator<INotifier> getNotifierIterator() {
        return notifiers.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Subscription && subscriptionId.equals(((Subscription) obj).subscriptionId);
    }

    @Override
    public int hashCode() {
        return subscriptionId.hashCode();
    }
}
