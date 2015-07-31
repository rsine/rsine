package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeSetFactory;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTestImmediateEval-context.xml"})
public class LabelUpdateTest {

    private final static URI conceptUri = new URIImpl("http://example.orf/concept1");    
    private final static CountingNotifier notifier = new CountingNotifier();
    
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;

    @Autowired
    private ChangeSetFactory changeSetFactory;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException, RepositoryException {
        notifier.reset();

        Model subscriptionModel = Helper.createModelFromResourceFile(
            "/internal/labelUpdateSubscription.ttl",
            RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel, true);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(notifier);
    }

    private void setLabels(URI concept1uri, URI concept2uri, int delay) throws RepositoryException, InterruptedException {        
        Statement defPrefLabel = new StatementImpl(concept1uri, SKOS.PREF_LABEL, new LiteralImpl("concept"));
        Statement updatePrefLabel = new StatementImpl(concept2uri, SKOS.PREF_LABEL, new LiteralImpl("updated concept"));

        persistAndNotifyProvider.persistAndNotify(
                changeSetFactory.assembleChangeset(ChangeSetFactory.StatementType.ADDITION, defPrefLabel),
                true);

        persistAndNotifyProvider.persistAndNotify(
                changeSetFactory.assembleChangeset(ChangeSetFactory.StatementType.REMOVAL, defPrefLabel),
                true);

        Thread.sleep(delay);

        persistAndNotifyProvider.persistAndNotify(
                changeSetFactory.assembleChangeset(ChangeSetFactory.StatementType.ADDITION, updatePrefLabel),
                true);
    }

    @Test
    public void performUpdateOnTime() throws RepositoryException, InterruptedException {
        setLabels(conceptUri, conceptUri, 1000);
        Assert.assertEquals(1, notifier.getNotificationCount());
    }

    @Test
    public void performUpdateTimediffTooLong() throws RepositoryException, InterruptedException {
        setLabels(conceptUri, conceptUri, 3000);
        Assert.assertEquals(0, notifier.getNotificationCount());
    }
    
    @Test
    public void noUpdate() throws RepositoryException, InterruptedException {
        setLabels(conceptUri, new URIImpl("http://example.orf/concept2"), 1000);
        Assert.assertEquals(0, notifier.getNotificationCount());
    }
    
}