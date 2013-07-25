package eu.lod2.rsine.querydispatcher;

import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class QueryDispatcher implements IQueryDispatcher {

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);

    private RegistrationService registrationService;
    private Repository repository;

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

        Iterator<String> queryIt = subscription.getQueryIterator();
        while (queryIt.hasNext()) {
            issueQueryAndNotify(queryIt.next(), subscription);
        }
    }

    private void issueQueryAndNotify(String query, Subscription subscription) throws RepositoryException
    {
        RepositoryConnection repCon = repository.getConnection();

        try {
            logger.debug("Issuing query '" +query+ "'");
            TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();

            while (result.hasNext()) {
                BindingSet bs = result.next();
                notifySubscriber(bs, subscription);
            }
        }
        catch (MalformedQueryException e) {
            logger.error("Query malformed", e);
        }
        catch (QueryEvaluationException e) {
            logger.error("Could not evaluate query", e);
        }
        finally {
            repCon.close();
        }
    }

    private void notifySubscriber(BindingSet bs, Subscription subscription) {
        logger.info("notifying subscriber: " +bs);
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
