package at.punkt.lod2.local;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.VelocityBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeSetCreator;
import eu.lod2.rsine.service.ChangeTripleService;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import eu.lod2.util.Namespaces;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
@DirtiesContext
public class NotificationWithAuxiliaryTest {

    private final URI conceptUri = new URIImpl("http://localhost/conceptA");
    private final URI otherConceptUri = new URIImpl("http://localhost/conceptB");
    private final Literal conceptUriLabel = new LiteralImpl("Concept A", "en");
    private final Literal otherConceptUriLabel = new LiteralImpl("Concept B", "en");
    private final String velocityTemplate = "Hierarchical relation between " +
        "<a href='$bindingSet.getValue('concept')'>$bindingSet.getValue('conceptLabel').getLabel()</a> and "+
        "<a href='$bindingSet.getValue('otherConcept')'>$bindingSet.getValue('otherConceptLabel').getLabel()</a>";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private Repository managedStoreRepo;

    private RepositoryConnection repCon;
    private MessageConcatenatingNotifier messageConcatenatingNotifier = new MessageConcatenatingNotifier();

    @Before
    public void setUp() throws IOException, RepositoryException {
        repCon = managedStoreRepo.getConnection();
        repCon.clear();

        addConceptData();

        registerSubscription(
                createBroaderRelationQuery(),
                new VelocityBindingSetFormatter(velocityTemplate));
    }

    private void addConceptData() throws RepositoryException {
        repCon.add(conceptUri, SKOS.BROADER, otherConceptUri);
        repCon.add(conceptUri, SKOS.PREF_LABEL, conceptUriLabel);
        repCon.add(otherConceptUri, SKOS.PREF_LABEL, otherConceptUriLabel);
    }

    private void registerSubscription(String query, BindingSetFormatter formatter) {
        Subscription subscription = new Subscription();

        NotificationQuery notificationQuery = new NotificationQuery(query, formatter, subscription);
        notificationQuery.addAuxiliaryQueries(Arrays.asList(createConceptLabelQuery(), createOtherConceptLabelQuery()));
        subscription.addQuery(notificationQuery);

        subscription.addNotifier(messageConcatenatingNotifier);
        registrationService.register(subscription, true);
    }

    private String createBroaderRelationQuery() {
        return Namespaces.SKOS_PREFIX+
                Namespaces.CS_PREFIX+
                Namespaces.DCTERMS_PREFIX+
                "SELECT ?concept ?otherConcept " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +

                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate skos:broader . " +
                    "?addition rdf:object ?otherConcept . "+

                    "FILTER (?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";
    }

    private String createConceptLabelQuery() {
        return Namespaces.SKOS_PREFIX + "SELECT ?conceptLabel WHERE {?concept skos:prefLabel ?conceptLabel}";
    }

    private String createOtherConceptLabelQuery() {
        return Namespaces.SKOS_PREFIX + "SELECT ?otherConceptLabel WHERE {?otherConcept skos:prefLabel ?otherConceptLabel}";
    }

    @After
    public void tearDown() throws RepositoryException {
        repCon.close();
    }

    @Test
    public void prefLabelsInMessage() throws IOException {
        persistChangeSet();

        Assert.assertTrue(messageConcatenatingNotifier.message.contains(conceptUriLabel.getLabel()));
        Assert.assertTrue(messageConcatenatingNotifier.message.contains(otherConceptUriLabel.getLabel()));
    }

    private void persistChangeSet() throws IOException {
        Model changeSet = new ChangeSetCreator().assembleChangeset(
            new StatementImpl(otherConceptUri, SKOS.BROADER, conceptUri),
            null,
            ChangeTripleService.CHANGETYPE_ADD);
        persistAndNotifyProvider.persistAndNotify(changeSet, true);
    }

    private class MessageConcatenatingNotifier implements INotifier {

        private String message = "";

        @Override
        public void notify(Collection<String> messages) {
            for (String message : messages) {
                this.message += message;
            }
        }

    }
}
