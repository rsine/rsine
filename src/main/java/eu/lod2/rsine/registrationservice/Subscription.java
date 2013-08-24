package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
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

    private static int id = 0;

    private URI subscriber;
    private Collection<NotificationQuery> queries;
    private Collection<INotifier> notifiers;

    public Subscription() {
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        subscriber = valueFactory.createURI(
            Namespaces.RSINE_NAMESPACE.getName(),
            "subscriber_" +id);
        queries = new HashSet<NotificationQuery>();
        notifiers = new ArrayList<INotifier>();
        id++;
    }

    public Subscription(URI subscriber) {        
        this.subscriber = subscriber;
        queries = new HashSet<NotificationQuery>();
        notifiers = new ArrayList<INotifier>();        
    }
    
    public URI getSubscriber() {
        return subscriber;
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

}
