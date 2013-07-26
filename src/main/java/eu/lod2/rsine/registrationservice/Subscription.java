package eu.lod2.rsine.registrationservice;

import eu.lod2.util.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class Subscription {

    private URI subscriber;
    private Collection<NotificationQuery> queries;

    Subscription() {
        ValueFactory valueFactory = new ValueFactoryImpl();
        subscriber = valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "subscriber_" +System.currentTimeMillis());
        queries = new HashSet<NotificationQuery>();
    }

    public URI getSubscriber() {
        return subscriber;
    }

    public void addQuery(String sparqlQuery) {
        queries.add(new NotificationQuery(sparqlQuery));
    }

    public Iterator<NotificationQuery> getQueryIterator() {
        return queries.iterator();
    }

}
