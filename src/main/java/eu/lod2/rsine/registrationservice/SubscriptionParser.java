package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.NotifierDescriptor;
import eu.lod2.rsine.dissemination.notifier.NotifierParameters;
import eu.lod2.util.Namespaces;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SubscriptionParser {

    private ValueFactory valueFactory = new ValueFactoryImpl();
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
        Collection<INotifier> notifiers = new ArrayList<>();

        Set<Value> allNotifiers = rdfSubscription.filter(
            null,
            valueFactory.createURI(Namespaces.RSINE_NAMESPACE.getName(), "notifier"),
            null).objects();
        for (Value notifier : allNotifiers) {
            NotifierDescriptor notifierDescriptor = getDescriptorByType();
            notifiers.add(notifierDescriptor.create(notifierDescriptor.getParameters()));
        }

        return notifiers;
    }

    private NotifierDescriptor getDescriptorByType() {
        return null;
    }

    private void setParameterValues(Resource notifier, NotifierParameters notifierParameters) {
    }

    private Collection<String> getSparqlQueries() {
        return Collections.EMPTY_LIST;
    }

}
