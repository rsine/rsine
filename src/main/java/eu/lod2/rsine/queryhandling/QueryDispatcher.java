package eu.lod2.rsine.queryhandling;

import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.queryhandling.policies.IEvaluationPolicy;
import eu.lod2.rsine.queryhandling.policies.ImmediateEvaluationPolicy;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class QueryDispatcher implements IQueryDispatcher {

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);
    private final int NUM_NOTIFY_THREADS = 15;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private QueryEvaluator queryEvaluator;

    @Autowired
    private PostponedQueryHandler postponedQueryHandler;

    @Autowired
    private IEvaluationPolicy evaluationPolicy;

    private ExecutorService notificationExecutor = Executors.newFixedThreadPool(NUM_NOTIFY_THREADS);
    private boolean asyncNotification;

    public QueryDispatcher() {

    }

    public QueryDispatcher(boolean asyncNotification) {
        this.asyncNotification = asyncNotification;
    }

    @Override
    public void trigger() {
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();
        if (!subscriptionIt.hasNext()) {
            logger.info("No subscribers registered");
        }
        try {
            while (subscriptionIt.hasNext()) {
                dispatchForSubscription(subscriptionIt.next());
            }
        }
        catch (RepositoryException e) {            
            logger.error("Error issuing query", e);
        }
    }

    private void dispatchForSubscription(Subscription subscription) throws RepositoryException {
        logger.debug("Dispatching queries for subscription with id '" + subscription.getSubscriptionId() + "'");
        Iterator<NotificationQuery> queryIt = subscription.getQueries();
        while (queryIt.hasNext()) {
            issueQueryAndNotify(queryIt.next());
        }
    }

    public synchronized void issueQueryAndNotify(NotificationQuery query) throws RepositoryException {
        issueQueryAndNotify(query, false);
    }

    public synchronized void issueQueryAndNotify(NotificationQuery query, boolean evaluateImmediately)
            throws RepositoryException
    {
        try {
            Collection<String> messages = queryEvaluator.evaluate(
                query,
                evaluateImmediately ? new ImmediateEvaluationPolicy() : evaluationPolicy);
            sendNotifications(messages, query.getSubscription());
            postponedQueryHandler.remove(query);
        }
        catch (OpenRDFException e) {
            logger.error("Could not evaluate query", e);
        }
        catch (EvaluationPostponedException e) {
            postponedQueryHandler.add(query);
        }
    }

    private void sendNotifications(Collection<String> messages, Subscription subscription) {
        Iterator<INotifier> notifierIt = subscription.getNotifierIterator();

        while (!messages.isEmpty() && notifierIt.hasNext()) {
            INotifier notifier = notifierIt.next();

            if (asyncNotification) {
                notificationExecutor.execute(new Notification(notifier, messages));
            }
            else {
                notifier.notify(messages);
            }
        }
    }

    private class Notification implements Runnable {

        private INotifier notifier;
        private Collection<String> messages;

        private Notification(INotifier notifier, Collection<String> messages) {
            this.notifier = notifier;
            this.messages = messages;
        }

        @Override
        public void run() {
            notifier.notify(messages);
        }

    }

}
