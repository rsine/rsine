package at.punkt.lod2.local;

import eu.lod2.rsine.dissemination.messageformatting.BindingSetFormatter;
import eu.lod2.rsine.dissemination.messageformatting.ToStringBindingSetFormatter;
import eu.lod2.rsine.dissemination.notifier.INotifier;
import eu.lod2.rsine.dissemination.notifier.logging.LoggingNotifier;
import eu.lod2.rsine.queryhandling.QueryEvaluator;
import eu.lod2.rsine.registrationservice.*;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import eu.lod2.util.Namespaces;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
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
    private final Literal conceptUriLabelEn = new LiteralImpl("Concept A", "en");
    private final Literal conceptUriLabelDe = new LiteralImpl("Konzept A", "de");
    private final Literal otherConceptUriLabelEn = new LiteralImpl("Concept B", "en");
    private final Literal otherConceptUriLabelDe = new LiteralImpl("Konzept B", "de");

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
                new ToStringBindingSetFormatter());
    }

    private void addConceptData() throws RepositoryException {
        repCon.add(conceptUri, SKOS.BROADER, otherConceptUri);
        repCon.add(conceptUri, SKOS.PREF_LABEL, conceptUriLabelEn);
        repCon.add(conceptUri, SKOS.PREF_LABEL, conceptUriLabelDe);
        repCon.add(otherConceptUri, SKOS.PREF_LABEL, otherConceptUriLabelEn);
        repCon.add(otherConceptUri, SKOS.PREF_LABEL, otherConceptUriLabelDe);
    }

    private void registerSubscription(String query, BindingSetFormatter formatter) {
        Subscription subscription = new Subscription();

        NotificationQuery notificationQuery = new NotificationQuery(query, formatter, subscription);

        notificationQuery.setAuxiliary(auxiliary);
        subscription.addQuery(notificationQuery);

        subscription.addNotifier(messageConcatenatingNotifier);
        registrationService.register(subscription, true);
    }

    private String createBroaderRelationQuery() {
        return Namespaces.SKOS_PREFIX+
                Namespaces.CS_PREFIX+
                Namespaces.DCTERMS_PREFIX+
                "SELECT ?sub ?obj " +
                "WHERE {" +
                    "?cs a cs:ChangeSet . " +
                    "?cs cs:createdDate ?csdate . " +
                    "?cs cs:addition ?addition . " +

                    "?addition rdf:subject ?concept . " +
                    "?addition rdf:predicate skos:broader . " +
                    "?addition rdf:object ?otherConcept . "+

                    "FILTER ((?csdate > \"" + QueryEvaluator.QUERY_LAST_ISSUED+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)" +
                "}";
    }

    @After
    public void tearDown() throws RepositoryException {
        repCon.close();
    }

    @Test
    public void prefLabelsInMessage() {
        //messageConcatenatingNotifier.message;
        Assert.fail();
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
