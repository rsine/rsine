package at.punkt.lod2.local;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.registrationservice.RegistrationService;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionExistsException;
import eu.lod2.rsine.registrationservice.SubscriptionNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Iterator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class RegistrationServiceTest {

    @Autowired
    private Helper helper;

    private RegistrationService registrationService;

    @Before
    public void setUp() {
        registrationService = new RegistrationService();
    }

    @Test
    public void registerSingleSubscription() throws RDFParseException, IOException, RDFHandlerException {
        int subscriptionsBeforeRegister = countSubscriptions();

        Model rdfSubscription = helper.createModelFromResourceFile("/internal/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
        registrationService.register(rdfSubscription);

        int subscriptionsAfterRegister = countSubscriptions();
        Assert.assertEquals(subscriptionsBeforeRegister + 1, subscriptionsAfterRegister);
    }

    private int countSubscriptions() {
        int count = 0;
        Iterator<Subscription> subscriptionIt = registrationService.getSubscriptionIterator();
        while (subscriptionIt.hasNext()) {
            subscriptionIt.next();
            count++;
        }
        return count;
    }

    @Test
    public void multipleSubscriptionsWithDifferentId() throws RDFParseException, IOException, RDFHandlerException {
        int subscriptionsBeforeRegister = countSubscriptions();

        int subscriptionCount = 5;
        for (int i = 0; i < subscriptionCount; i++) {
            Model rdfSubscription = helper.createModelFromResourceFile("/internal/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
            registrationService.register(rdfSubscription);
        }

        Assert.assertEquals(subscriptionsBeforeRegister + subscriptionCount, countSubscriptions());
    }

    @Test(expected = SubscriptionExistsException.class)
    public void multipleSubscriptionWithSameId() throws RDFParseException, IOException, RDFHandlerException {
        Model subscription = helper.createModelFromResourceFile("/internal/subscriptionWithUri.ttl", RDFFormat.TURTLE);
        Model identicalSubscription = helper.createModelFromResourceFile("/internal/subscriptionWithUri.ttl", RDFFormat.TURTLE);

        registrationService.register(subscription);
        registrationService.register(identicalSubscription);
    }

    @Test
    public void unregister() throws RDFParseException, IOException, RDFHandlerException {
        int subscriptionsBeforeRegister = countSubscriptions();

        Model subscription = helper.createModelFromResourceFile("/internal/subscriptionWithUri.ttl", RDFFormat.TURTLE);
        registrationService.register(subscription);

        int subscriptionsAfterRegister = countSubscriptions();
        registrationService.unregister(new URIImpl("http://example.org/someSubscription"));

        int subscriptionsAfterUnregister = countSubscriptions();

        Assert.assertEquals(subscriptionsBeforeRegister + 1, subscriptionsAfterRegister);
        Assert.assertEquals(subscriptionsBeforeRegister, subscriptionsAfterUnregister);
    }


    @Test(expected = SubscriptionNotFoundException.class)
    public void unregisterNonExistingSubscription() throws RDFParseException, IOException, RDFHandlerException
    {
        Model subscription = helper.createModelFromResourceFile("/internal/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
        registrationService.register(subscription);
        registrationService.unregister(new URIImpl("http://example.org/someSubscription"));
    }

}
