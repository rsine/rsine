package at.punkt.lod2.local;

import at.punkt.lod2.util.Helper;
import eu.lod2.rsine.dissemination.messageformatting.VelocityBindingSetFormatter;
import eu.lod2.rsine.registrationservice.NotificationQuery;
import eu.lod2.rsine.registrationservice.Subscription;
import eu.lod2.rsine.registrationservice.SubscriptionParser;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;

public class SubscriptionParserTest {

    @Test
    public void emailNotificationSubscription() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = Helper.createModelFromResourceFile("/internal/emailNotifierSubscription.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        Assert.assertTrue(subscription.getNotifierIterator().hasNext());
        Assert.assertTrue(subscription.getQueries().hasNext());
    }

    @Test
    public void customFormatter() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = Helper.createModelFromResourceFile("/internal/labelChangeSubscriptionFormatted.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        NotificationQuery notificationQuery = subscription.getQueries().next();
        Assert.assertNotNull(notificationQuery);
        Assert.assertTrue(notificationQuery.getBindingSetFormatter() instanceof VelocityBindingSetFormatter);
    }

    @Test
    public void queryWithCondition() throws RDFParseException, IOException, RDFHandlerException {
        Model rdfSubscription = Helper.createModelFromResourceFile("/internal/subscriptionWithCondition.ttl", RDFFormat.TURTLE);
        Subscription subscription = new SubscriptionParser(rdfSubscription).createSubscription();

        NotificationQuery notificationQuery = subscription.getQueries().next();
        Assert.assertNotNull(notificationQuery);
        Assert.assertTrue(notificationQuery.getConditions().hasNext());
    }

}
