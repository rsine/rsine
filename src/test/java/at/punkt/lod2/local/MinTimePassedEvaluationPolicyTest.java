package at.punkt.lod2.local;

import com.jayway.awaitility.Awaitility;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeSetFactory;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import eu.lod2.util.Namespaces;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"MinTimePassedEvaluationPolicyTest-context.xml"})
@DirtiesContext
public class MinTimePassedEvaluationPolicyTest  {

    private final long IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS = 1000;

    private TimeMeasureNotifier timeMeasureNotifier = new TimeMeasureNotifier();

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ChangeSetFactory changeSetFactory;

    @Before
    public void setUp() throws IOException, RepositoryException, RDFParseException, QueryEvaluationException, MalformedQueryException {
        Subscription subscription = new Subscription();
        subscription.addQuery(new NotificationQuery(createQuery(), new ToStringBindingSetFormatter(), subscription));
        subscription.addNotifier(timeMeasureNotifier);
        registrationService.register(subscription, true);
    }

    private String createQuery() {
        //preflabel changes of concepts
        return Namespaces.SKOS_PREFIX+
               Namespaces.CS_PREFIX+
               Namespaces.DCTERMS_PREFIX+
               "SELECT * " +
                    "WHERE {" +
                        "?cs a cs:ChangeSet . " +
                        "?cs cs:createdDate ?csdate . " +
                        "?cs cs:removal ?removal . " +
                        "?cs cs:addition ?addition . " +
                        "?removal rdf:subject ?concept . " +
                        "?addition rdf:subject ?concept . " +
                        "?removal rdf:predicate skos:prefLabel . " +
                        "?removal rdf:object ?oldLabel . "+
                        "?addition rdf:predicate skos:prefLabel . " +
                        "?addition rdf:object ?newLabel . "+
                        "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                    "}";
    }

    protected void changePrefLabel() throws IOException {
        Statement oldStatement = new StatementImpl(
                new URIImpl("http://reegle.info/glossary/1111"),
                new URIImpl("http://www.w3.org/2004/02/skos/core#prefLabel"),
                new LiteralImpl("Ottakringer Helles", "en"));

        Statement newStatement = new StatementImpl(
                new URIImpl("http://reegle.info/glossary/1111"),
                new URIImpl("http://www.w3.org/2004/02/skos/core#prefLabel"),
                new LiteralImpl("Schremser Edelmärzen", "en"));

        Model updateChangeSet = changeSetFactory.assembleChangeset(
                Arrays.asList(newStatement), Arrays.asList(oldStatement));
        persistAndNotifyProvider.persistAndNotify(updateChangeSet, true);
    }

    @Test
    public void immediateNotificationOnFirstChange() throws IOException {
        performChange();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
        Assert.assertTrue(timeMeasureNotifier.millisPassed < IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS);
    }

    private void performChange() throws IOException {
        timeMeasureNotifier.reset();
        changePrefLabel();
    }

    @Test
    public void changeTooSoonForNotification() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        performChange();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
        Assert.assertTrue(timeMeasureNotifier.getMillisPassed() > IMMEDIATE_NOTIFICATION_THRESHOLD_MILLIS);
    }

    @Test
    public void changeLateEnoughForNotification() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        try {
            Thread.sleep(6000);
        }
        catch (InterruptedException e) {
            // ignore
        }

        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
    }

    @Test
    public void lastChangeNotMissed() throws IOException {
        performChange();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));

        performChange();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(new NotificationDetector(timeMeasureNotifier));
    }

    private class NotificationDetector implements Callable<Boolean> {

        private TimeMeasureNotifier notifier;

        private NotificationDetector(TimeMeasureNotifier notifier) {
            this.notifier = notifier;
        }

        public Boolean call() throws Exception {
            return notifier.getMillisPassed() != null;
        }
    }

    private class TimeMeasureNotifier implements INotifier {

        private long time = 0;
        private Long millisPassed = null;

        private void reset() {
            millisPassed = null;
            time = System.currentTimeMillis();
        }

        public synchronized void notify(Collection<String> messages) {
            millisPassed = System.currentTimeMillis() - time;
        }

        public synchronized Long getMillisPassed() {
            return millisPassed;
        }

    }

}
