package eu.lod2.rsine.querydispatcher;

import eu.lod2.rsine.dissemination.Notifier;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Iterator;

public class QueryDispatcher implements IQueryDispatcher {

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);
    public final static String QUERY_LAST_ISSUED = "QUERY_LAST_ISSUED";

    private RegistrationService registrationService;
    private Repository repository;
    private Notifier notifier;

    @Override
    public void trigger() {
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();

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
        logger.info("Dispatching queries for subscriber '" +subscription.getSubscriber()+ "'");

        Iterator<NotificationQuery> queryIt = subscription.getQueryIterator();
        while (queryIt.hasNext()) {
            issueQueryAndNotify(queryIt.next(), subscription);
        }
    }

    private void issueQueryAndNotify(NotificationQuery query, Subscription subscription) throws RepositoryException
    {
        RepositoryConnection repCon = repository.getConnection();

        try {
            logger.debug("Issuing query '" +query+ "'");

            TupleQueryResult result = repCon.prepareTupleQuery(
                QueryLanguage.SPARQL,
                amendChangeSetsTimeConstraint(query)).evaluate();

            while (result.hasNext()) {
                BindingSet bs = result.next();
                notifier.queryResultsAvailable(bs, subscription);
            }

            query.updateLastIssued();

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

    /**
     * Replaces the placeholder in the subscriber query with the date the query has been last issued. This way only
     * changesets that have an creation date after the query hast been last issued are returned
     */
    private String amendChangeSetsTimeConstraint(NotificationQuery query) {
        String sparqlQuery = query.getSparqlQuery();
        String queryLastIssuedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(query.getLastIssued());
        return sparqlQuery.replace(QUERY_LAST_ISSUED, queryLastIssuedDate);
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

}
