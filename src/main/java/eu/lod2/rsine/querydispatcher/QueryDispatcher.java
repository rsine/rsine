package eu.lod2.rsine.querydispatcher;

import eu.lod2.rsine.changesetstore.ChangeSetStore;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class QueryDispatcher implements IQueryDispatcher {

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);

    public final static String QUERY_LAST_ISSUED = "QUERY_LAST_ISSUED";
    public final static String MANAGED_STORE_SPARQL_ENDPONT = "MANAGED_STORE_SPARQL_ENDPONT";

    private RegistrationService registrationService;
    private ChangeSetStore changeSetStore;
    private String managedTripleStoreSparqlEndpoint = "";

    @Override
    public void trigger() {
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();
        if (!subscriptionIt.hasNext()) {
            logger.info("No subscribers registered");
        }

        try {
            while (subscriptionIt.hasNext()) {
                dispatchForSubscriber(subscriptionIt.next());
            }
        }
        catch (RepositoryException e) {
            logger.error("Error issuing query", e);
        }
    }

    public void dispatchForSubscriber(Subscription subscription) throws RepositoryException {
        logger.debug("Dispatching queries for subscriber '" + subscription.getSubscriber() + "'");

        Iterator<NotificationQuery> queryIt = subscription.getQueryIterator();
        while (queryIt.hasNext()) {
            issueQueryAndNotify(queryIt.next(), subscription);
        }
    }

    private void issueQueryAndNotify(NotificationQuery query, Subscription subscription) throws RepositoryException
    {
        RepositoryConnection repCon = changeSetStore.getRepository().getConnection();

        try {
            String issuedQuery = fillInPlaceholders(query);
            logger.debug("Issuing query '" +issuedQuery+ "'");

            TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, issuedQuery).evaluate();

            List<String> messages = new ArrayList<>();
            while (result.hasNext()) {
                BindingSet bs = result.next();
                messages.add(query.getBindingSetFormatter().toMessage(bs));
            }
            query.updateLastIssued();

            sendNotifications(messages, subscription);
        }
        catch (MalformedQueryException e) {
            logger.error("NotificationQuery malformed", e);
        }
        catch (QueryEvaluationException e) {
            logger.error("Could not evaluate query", e);
        }
        finally {
            repCon.close();
        }
    }

    private void sendNotifications(Collection<String> messages, Subscription subscription) {
        Iterator<INotifier> notifierIt = subscription.getNotifierIterator();
        while (!messages.isEmpty() && notifierIt.hasNext()) {
            INotifier notifier = notifierIt.next();
            notifier.notify(messages);
        }
    }

    private String fillInPlaceholders(NotificationQuery query) {
        String sparqlQuery;
        sparqlQuery = amendChangeSetsTimeConstraint(query);
        sparqlQuery = amendManagedTripleStoreURIs(sparqlQuery);
        return sparqlQuery;
    }

    /**
     * Replaces the placeholder in the subscriber query with the date the query has been last issued. This way only
     * changesets that have an creation date after the query hast been last issued are returned
     */
    private String amendChangeSetsTimeConstraint(NotificationQuery query) {
        String sparqlQuery = query.getSparqlQuery();
        String queryLastIssuedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(query.getLastIssued());
        return sparqlQuery.replace(QUERY_LAST_ISSUED, queryLastIssuedDate);
    }

    private String amendManagedTripleStoreURIs(String query) {
        return query.replace(MANAGED_STORE_SPARQL_ENDPONT, managedTripleStoreSparqlEndpoint);
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void setChangeSetStore(ChangeSetStore changeSetStore) {
        this.changeSetStore = changeSetStore;
    }

    public void setManagedTripleStore(String sparqlEndpoint) {
        managedTripleStoreSparqlEndpoint = sparqlEndpoint;
    }

}
