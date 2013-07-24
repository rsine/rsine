package eu.lod2.rsine.registrationservice;

import eu.lod2.util.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.Collection;
import java.util.HashSet;

public class Subscription {

    private URI subscriber;
    private Collection<String> queries;

    Subscription() {
        ValueFactory valueFactory = new ValueFactoryImpl();
        subscriber = valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "subscriber_" +System.currentTimeMillis());
        queries = new HashSet<String>();
    }

    public URI getSubscriber() {
        return subscriber;
    }

    public void addQuery(String query) {
        queries.add(query);
    }

}
