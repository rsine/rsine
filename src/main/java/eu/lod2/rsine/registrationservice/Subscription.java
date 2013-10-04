package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.util.Namespaces;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class Subscription {

    private static int id = 0;

    private Resource subscriptionId;
    private Collection<NotificationQuery> queries;
    private Collection<INotifier> notifiers;

    public Subscription() {
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        subscriptionId = valueFactory.createURI(
            Namespaces.RSINE_NAMESPACE.getName(),
            "subscriber_" +id);
        queries = new HashSet<NotificationQuery>();
        notifiers = new ArrayList<INotifier>();
        notifiers.add(new LoggingNotifier());
        id++;
    }

    public Resource getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Resource subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void addQuery(String sparqlQuery) {
        queries.add(new NotificationQuery(sparqlQuery, new ToStringBindingSetFormatter()));
    }

    public void addQuery(String sparqlQuery, BindingSetFormatter bindingSetFormatter) {
        queries.add(new NotificationQuery(sparqlQuery, bindingSetFormatter));
    }

    public Iterator<NotificationQuery> getQueryIterator() {
        return queries.iterator();
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
