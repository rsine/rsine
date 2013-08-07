package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.util.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class Subscription {

    private URI subscriber;
    private Collection<NotificationQuery> queries;
    private Collection<INotifier> notifiers;

    Subscription() {
        ValueFactory valueFactory = new ValueFactoryImpl();
        subscriber = valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "subscriber_" +System.currentTimeMillis());
        queries = new HashSet<>();
        notifiers = new ArrayList<>();
    }

    public URI getSubscriber() {
        return subscriber;
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

}
