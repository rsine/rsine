package eu.lod2.rsine.registrationservice;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.NotifierDescriptor;
import eu.lod2.rsine.dissemination.notifier.NotifierParameters;
import eu.lod2.util.ItemNotFoundException;
import eu.lod2.util.Namespaces;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import java.util.*;

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
            URI type = rdfSubscription.filter((Resource) notifier, RDF.TYPE, null).objectURI();

            NotifierDescriptor notifierDescriptor = getDescriptorByType(type);
            NotifierParameters notifierParameters = setParameterValues((Resource) notifier, notifierDescriptor.getParameters());
            notifiers.add(notifierDescriptor.create(notifierParameters));
        }

        return notifiers;
    }

    private NotifierDescriptor getDescriptorByType(URI type) {
        ServiceLoader<NotifierDescriptor> loader = ServiceLoader.load(NotifierDescriptor.class);
        Iterator<NotifierDescriptor> it = loader.iterator();
        while (it.hasNext()) {
            NotifierDescriptor notifierDescriptor = it.next();
            if (notifierDescriptor.getType().equals(type)) return notifierDescriptor;
        }

        throw  new ItemNotFoundException("No notifier descriptor with type '" +type+ "' registered");
    }

    private NotifierParameters setParameterValues(Resource notifier, NotifierParameters notifierParameters) {
        Iterator<NotifierParameters.NotifierParameter> parameterIterator = notifierParameters.getParameterIterator();

        while (parameterIterator.hasNext()) {
            NotifierParameters.NotifierParameter notifierParameter = parameterIterator.next();
            Value value = getValueOfPredicate(notifierParameter.getId(), notifier);
            notifierParameter.setValue(value);
        }

        return notifierParameters;
    }

    private Value getValueOfPredicate(URI predicate, Resource notifier) {
        return rdfSubscription.filter(notifier, predicate, null).objectValue();
    }

    private Collection<String> getSparqlQueries() {
        return Collections.EMPTY_LIST;
    }

}
