package eu.lod2.rsine.queryhandling;

import eu.lod2.rsine.registrationservice.NotificationQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PostponedQueryHandler {

    @Autowired
    private QueryDispatcher queryDispatcher;

    private final Logger logger = LoggerFactory.getLogger(QueryDispatcher.class);
    private Set<NotificationQuery> postponedQueries = new HashSet<NotificationQuery>();

    public synchronized void add(NotificationQuery notificationQuery) {
        postponedQueries.add(notificationQuery);
        logger.info("Query postponed; " +inQueue());
    }

    private String inQueue() {
        return postponedQueries.size()+ " pending";
    }

    public synchronized void remove(NotificationQuery notificationQuery) {
        postponedQueries.remove(notificationQuery);
        logger.info("Postponed query processed; " +inQueue());
    }

    public void cleanUp() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        logger.info("Cleaning up postponed queries; " +(postponedQueries.isEmpty() ? "nothing to do" : inQueue()));
        for (NotificationQuery query : postponedQueries) {
            query.resetLastIssued();
            queryDispatcher.issueQueryAndNotify(query);
            remove(query);
        }
        logger.info("Postponed query cleanup finished");
    }

}
