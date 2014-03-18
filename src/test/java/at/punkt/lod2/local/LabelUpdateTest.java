package at.punkt.lod2.local;

import at.punkt.lod2.util.CountingNotifier;
import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.service.ChangeTripleService;
import eu.lod2.rsine.service.PersistAndNotifyProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import org.junit.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTestImmediateEval-context.xml"})
public class LabelUpdateTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PersistAndNotifyProvider persistAndNotifyProvider;
    
    private CountingNotifier notifier;

    @Before
    public void setUp() throws IOException, RDFParseException, RDFHandlerException, RepositoryException {
        notifier = new CountingNotifier();
        subscribe();
    }

    private void subscribe() throws RDFParseException, IOException, RDFHandlerException {
        Model subscriptionModel = Helper.createModelFromResourceFile(
            "/internal/labelUpdateSubscription.ttl",
            RDFFormat.TURTLE);
        Resource subscriptionId = registrationService.register(subscriptionModel, true);
        Subscription subscription = registrationService.getSubscription(subscriptionId);
        subscription.addNotifier(notifier);
    }

    private void setLabels(int delay) throws RepositoryException, InterruptedException {
        URI conceptUri = new URIImpl("http://example.orf/concept1");

        Statement defPrefLabel = new StatementImpl(conceptUri, SKOS.PREF_LABEL, new LiteralImpl("concept"));
        Statement updatePrefLabel = new StatementImpl(conceptUri, SKOS.PREF_LABEL, new LiteralImpl("updated concept"));

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(defPrefLabel, ChangeTripleService.CHANGETYPE_ADD),
                true);

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(defPrefLabel, ChangeTripleService.CHANGETYPE_REMOVE),
                true);

        Thread.sleep(delay);

        persistAndNotifyProvider.persistAndNotify(
                Helper.createChangeSetModel(updatePrefLabel, ChangeTripleService.CHANGETYPE_ADD),
                true);
    }

    @Test
    public void performUpdateOnTime() throws RepositoryException, InterruptedException {
        setLabels(1000);
        Assert.assertEquals(1, notifier.getNotificationCount());
    }

    @Test
    public void performUpdateTimediffTooLong() throws RepositoryException, InterruptedException {
        setLabels(3000);
        Assert.assertEquals(0, notifier.getNotificationCount());
    }
    
}