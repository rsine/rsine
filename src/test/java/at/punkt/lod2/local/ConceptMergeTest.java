package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.Rsine;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeTripleService;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
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

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException, RepositoryException {
        managedStoreRepo.getConnection().add(Rsine.class.getResource("/reegle.rdf"), "", RDFFormat.RDFXML);
        subscribe();
    }

    private void subscribe() throws RDFParseException, IOException, RDFHandlerException {
        countingNotifier = new CountingNotifier();

        Model subscriptionModel = Helper.createModelFromResourceFile("/wk/subscription_pp_merge.ttl", RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(countingNotifier);
    }

    @Test
    public void mergeDetection() throws RepositoryException {
        String mainConcept = "http://reegle.info/glossary/440";
        String abandonedConcept = "http://reegle.info/glossary/422";
        Literal abandonedConceptPrefLabel = new LiteralImpl("combi storage tanks", "en");

        Helper.setLabel(managedStoreRepo.getConnection(),
                new URIImpl("http://reegle.info/glossary/1111"),
                SKOS.PREF_LABEL,
                new LiteralImpl("Ottakringer Helles", "en"),
                persistAndNotifyProvider);
        Helper.setAltLabel(managedStoreRepo.getConnection(),
                new URIImpl(mainConcept),
                abandonedConceptPrefLabel,
                persistAndNotifyProvider);
        removeConcept(new URIImpl(abandonedConcept));

        Assert.assertEquals(1, countingNotifier.getNotificationCount());
    }

    private void removeConcept(URI concept) throws RepositoryException {
        managedStoreRepo.getConnection().add(concept, new URIImpl(OWL.NAMESPACE + "deprecated"), new BooleanLiteralImpl(true));

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(concept.stringValue(),
                        OWL.NAMESPACE + "deprecated",
                        new BooleanLiteralImpl(true),
                        ChangeTripleService.CHANGETYPE_ADD),
                true);
    }

}
