package at.punkt.lod2.local;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.dissemination.messageformatting.VelocityBindingSetFormatter;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"LocalTest-context.xml"})
public class SubscriptionParserTest {

    @Autowired
    private Helper helper;

    @Test
    public void emailNotificationSubscription() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = helper.createModelFromResourceFile("/internal/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        Assert.assertTrue(subscription.getNotifierIterator().hasNext());
        Assert.assertTrue(subscription.getQueries().hasNext());
    }

    @Test
    public void customFormatter() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = helper.createModelFromResourceFile("/internal/labelChangeSubscriptionFormatted.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        NotificationQuery notificationQuery = subscription.getQueries().next();
        Assert.assertNotNull(notificationQuery);
        Assert.assertTrue(notificationQuery.getBindingSetFormatter() instanceof VelocityBindingSetFormatter);
    }

    @Test
    public void queryWithCondition() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = helper.createModelFromResourceFile("/internal/subscriptionWithCondition.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        NotificationQuery notificationQuery = subscription.getQueries().next();
        Assert.assertNotNull(notificationQuery);
        Assert.assertTrue(notificationQuery.getConditions().hasNext());
    }

}
