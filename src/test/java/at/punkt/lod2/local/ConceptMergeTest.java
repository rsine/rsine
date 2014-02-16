package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeTripleService;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.*;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
@DirtiesContext
public class ConceptMergeTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private Repository managedStoreRepo;

    private CountingNotifier countingNotifier;
    private RepositoryConnection repCon;
    private URI mainConceptUri, abandonedConceptUri;
    private Literal abandonedConceptPrefLabel;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException, RepositoryException {
        repCon = managedStoreRepo.getConnection();
        addConcepts();
        subscribe();
    }

    private void addConcepts() throws RepositoryException {
        mainConceptUri = new URIImpl("http://reegle.info/glossary/440");
        abandonedConceptUri = new URIImpl("http://reegle.info/glossary/442");
        abandonedConceptPrefLabel = new LiteralImpl("combi storage tanks", "en");

        Statement mainConcept = new StatementImpl(mainConceptUri, RDF.TYPE, SKOS.CONCEPT);
        Statement abandonedConcept = new StatementImpl(abandonedConceptUri, RDF.TYPE, SKOS.CONCEPT);
        Statement abandonedConceptLabel = new StatementImpl(abandonedConceptUri, SKOS.PREF_LABEL, abandonedConceptPrefLabel);
        repCon.add(mainConcept);
        repCon.add(abandonedConcept);
        repCon.add(abandonedConceptLabel);
    }


    private void subscribe() throws RDFParseException, IOException, RDFHandlerException {
        countingNotifier = new CountingNotifier();

        Model subscriptionModel = Helper.createModelFromResourceFile("/wk/subscription_pp_merge.ttl", RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel, true);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(countingNotifier);
    }

    @After
    public void tearDown() throws RepositoryException {
        repCon.close();
    }

    @Test
    public void mergeDetection() throws RepositoryException {
        Helper.setLabel(repCon,
                new URIImpl("http://reegle.info/glossary/1111"),
                SKOS.PREF_LABEL,
                new LiteralImpl("Ottakringer Helles", "en"),
                persistAndNotifyProvider);
        Helper.setAltLabel(repCon, mainConceptUri, abandonedConceptPrefLabel, persistAndNotifyProvider);
        removeConcept(abandonedConceptUri);

        Assert.assertEquals(1, countingNotifier.getNotificationCount());
    }

    private void removeConcept(URI concept) throws RepositoryException {
        repCon.add(concept, new URIImpl(OWL.NAMESPACE + "deprecated"), new BooleanLiteralImpl(true));

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(concept.stringValue(),
                        OWL.NAMESPACE + "deprecated",
                        new BooleanLiteralImpl(true),
                        ChangeTripleService.CHANGETYPE_ADD),
                true);
    }

}
