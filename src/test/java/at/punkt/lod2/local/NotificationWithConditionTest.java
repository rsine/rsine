package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import eu.lod2.rsine.changesetservice.ChangeSetCreator;
import eu.lod2.rsine.changesetservice.ChangeTripleHandler;
import eu.lod2.rsine.changesetservice.PersistAndNotifyProvider;
import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.Condition;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
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
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class NotificationWithConditionTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private Repository managedStoreRepo;

    private CountingNotifier countingNotifier;

    private Statement prefLabelStatement = new StatementImpl(
        new URIImpl("http://reegle.info/glossary/someConcept"),
        new URIImpl("http://www.w3.org/2004/02/skos/core#prefLabel"),
        new LiteralImpl("some preflabel", "en"));

    @Before
    public void setUp() throws IOException, RepositoryException {
        managedStoreRepo.getConnection().clear();
        countingNotifier = new CountingNotifier();
    }

    @Test
    public void propertyCreated()
        throws IOException, RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        registerSubscription(
                createPropertyCreatedQuery(),
                new ToStringBindingSetFormatter(),
                new Condition(createPrefLabelCondition(), false)); // triple did not exist before

        persistChangeSet();
        managedStoreRepo.getConnection().add(prefLabelStatement);

        Assert.assertEquals(1, countingNotifier.getNotificationCount());
    }

    private void registerSubscription(String query, BindingSetFormatter formatter, Condition condition) {
        Subscription subscription = new Subscription();
        subscription.addQuery(query, formatter, condition);
        subscription.addNotifier(new LoggingNotifier());
        subscription.addNotifier(countingNotifier);
        registrationService.register(subscription, true);
    }

    private String createPropertyCreatedQuery() {
        return Namespaces.SKOS_PREFIX+
                Namespaces.CS_PREFIX+
                Namespaces.DCTERMS_PREFIX+
                "SELECT ?sub ?obj " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +

                    "?addition rdf:subject ?sub . " +
                    "?addition rdf:predicate ?pre . " +
                    "?addition rdf:object ?obj . "+

                    "FILTER ((?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) && " +
                    "(?pre IN (skos:prefLabel)))" +
                "}";
    }

    private String createPrefLabelCondition() {
        return Namespaces.SKOS_PREFIX + "ASK {?sub skos:prefLabel ?obj}";
    }

    private void persistChangeSet() throws IOException {
        Model changeSet = new ChangeSetCreator().assembleChangeset(
            prefLabelStatement,
            null,
            ChangeTripleHandler.CHANGETYPE_ADD);
        persistAndNotifyProvider.persistAndNotify(changeSet, true);
    }

    @Test
    public void propertyChanged()
        throws IOException, MalformedQueryException, RepositoryException, UpdateExecutionException
    {
        registerSubscription(
                createPropertyCreatedQuery(),
                new ToStringBindingSetFormatter(),
                new Condition(createPrefLabelCondition(), true)); // triple did exist before

        persistChangeSet(); // no notification should occur here because condition is not fulfilled

        managedStoreRepo.getConnection().add(prefLabelStatement);
        persistChangeSet(); // here we get the one and only notification

        Assert.assertEquals(1, countingNotifier.getNotificationCount());
    }

}
